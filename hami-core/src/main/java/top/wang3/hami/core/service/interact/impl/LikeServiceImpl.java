package top.wang3.hami.core.service.interact.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.support.TransactionTemplate;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.dto.interact.LikeType;
import top.wang3.hami.common.message.interact.LikeRabbitMessage;
import top.wang3.hami.common.model.LikeItem;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RandomUtils;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.annotation.CostLog;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.component.ZPageHandler;
import top.wang3.hami.core.exception.HamiServiceException;
import top.wang3.hami.core.service.article.repository.ArticleRepository;
import top.wang3.hami.core.service.article.repository.ArticleStatRepository;
import top.wang3.hami.core.service.comment.repository.CommentRepository;
import top.wang3.hami.core.service.interact.LikeService;
import top.wang3.hami.core.service.interact.repository.LikeRepository;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@SuppressWarnings("all")
//@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final ArticleStatRepository articleStatRepository;
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final RabbitMessagePublisher rabbitMessagePublisher;
    private final TransactionTemplate transactionTemplate;


    @Override
    public boolean doLike(Integer itemId, LikeType likeType) {
        int loginUserId = LoginUserContext.getLoginUserId();
        int itemUser = getItemUser(itemId, likeType);
        //检查合法性后，可以直接发消息然后再写持久层, 然后消费binlog同步Redis
        Boolean success = transactionTemplate.execute(status -> {
            boolean success1 = likeRepository.doLike(loginUserId, itemId, likeType);
            if (success1 && addLikeCount(itemId, likeType)) {
                return true;
            }
            status.setRollbackOnly();
            return false;
        });
        if (!Boolean.TRUE.equals(success)) return false;
        LikeRabbitMessage message = new LikeRabbitMessage(loginUserId, itemUser, Constants.ONE, itemId, likeType);
        rabbitMessagePublisher.publishMsg(message);
        return true;
    }

    @Override
    public boolean cancelLike(Integer itemId, LikeType likeType) {
        int loginUserId = LoginUserContext.getLoginUserId();
        int itemUser = getItemUser(itemId, likeType);
        Boolean success = transactionTemplate.execute(status -> {
            boolean success1 = likeRepository.cancelLike(loginUserId, itemId, likeType);
            if (success1 && reduceLikeCount(itemId, likeType)) {
                return true;
            }
            status.setRollbackOnly();
            return false;
        });
        if (!Boolean.TRUE.equals(success)) return false;
        LikeRabbitMessage message = new LikeRabbitMessage(loginUserId, itemUser, Constants.ZERO, itemId, likeType);
        rabbitMessagePublisher.publishMsg(message);
        return true;
    }

    @CostLog
    @Override
    public Long getUserLikeCount(Integer userId, LikeType likeType) {
        //获取用户点赞的实体数 (我赞过)
        String key = RedisConstants.USER_LIKE_COUNT + likeType.getType() + ":" + userId;
        Long count = RedisClient.getCacheObject(key);
        if (count != null) {
            return count;
        }
        synchronized (this) {
            count = RedisClient.getCacheObject(key);
            if (count == null) {
                count = likeRepository.queryUserLikeItemCount(userId, likeType);
                RedisClient.setCacheObject(key, count, RandomUtils.randomLong(10, 20), TimeUnit.DAYS);
            }
            return count;
        }
    }

    @CostLog
    @Override
    public Collection<Integer> listUserLikeArticles(Page<LikeItem> page, Integer userId) {
        //最近点赞的文章
        String key = buildKey(userId, LikeType.ARTICLE);
        return ZPageHandler.<Integer>of(key, page, this) //不同业务不同的锁
                .countSupplier(() -> {
                    return getUserLikeCount(userId, LikeType.ARTICLE);
                })
                .loader((c, s) -> {
                    return loadUserLikeItem(key, userId, LikeType.ARTICLE, c, s);
                })
                .query();
    }

    @Override
    public boolean hasLiked(Integer userId, Integer itemId, LikeType likeType) {
        //两次redis操作
        //感觉可以用hash结构存储ttl和点赞的item
        //判断ttl过期则重新加载 减少一次io
        Map<Integer, Boolean> liked = hasLiked(userId, List.of(itemId), likeType);
        return liked.getOrDefault(itemId, false);
    }


    @CostLog
    @Override
    public Map<Integer, Boolean> hasLiked(Integer userId, List<Integer> itemIds, LikeType likeType) {
        String key = buildKey(userId, likeType);
        if (getUserLikeCount(userId, likeType) == 0) {
            return Collections.emptyMap();
        }
        boolean success = RedisClient.expire(key, RandomUtils.randomLong(10, 100), TimeUnit.HOURS);
        if (!success) {
            synchronized (this) {
                success = RedisClient.expire(key, RandomUtils.randomLong(10, 100), TimeUnit.HOURS);
                if (success) {
                    return RedisClient.zMContains(key, itemIds);
                } else {
                    loadUserLikeItem(key, userId, likeType, -1, -1);
                }
            }
        }
        return RedisClient.zMContains(key, itemIds);
    }

    @Override
    public List<Integer> loadUserLikeItem(String key, Integer userId, LikeType likeType, long current, long size) {
        List<LikeItem> likeItems = likeRepository.listUserLikeItem(userId, likeType);
        var tuples = ListMapperHandler.listToZSet(likeItems, LikeItem::getItemId, item -> {
            return (double) item.getMtime().getTime();
        });
        if (!tuples.isEmpty()) {
            RedisClient.deleteObject(key);
            RedisClient.zAddAll(key, tuples);
            RedisClient.expire(key, RandomUtils.randomLong(10, 100), TimeUnit.HOURS);
        }
        return ListMapperHandler.subList(likeItems, LikeItem::getItemId, current, size);
    }

    private int getItemUser(Integer itemId, LikeType likeType) {
        Integer itemUser = null;
        if (LikeType.ARTICLE.equals(likeType)) {
            itemUser = articleRepository.getArticleAuthor(itemId);
        } else if (LikeType.COMMENT.equals(likeType)) {
            itemUser = commentRepository.getCommentUser(itemId);
        }
        if (itemUser == null) throw new HamiServiceException("参数错误");
        return itemUser;
    }

    private boolean addLikeCount(Integer itemId, LikeType likeType) {
        if (LikeType.ARTICLE.equals(likeType)) {
            return articleStatRepository.increaseLikes(itemId, 1);
        } else if (LikeType.COMMENT.equals(likeType)) {
            return commentRepository.increaseLikes(itemId);
        }
        return false;
    }

    private boolean reduceLikeCount(Integer itemId, LikeType likeType) {
        if (LikeType.ARTICLE.equals(likeType)) {
            return articleStatRepository.decreaseLikes(itemId, 1);
        } else if (LikeType.COMMENT.equals(likeType)) {
            return commentRepository.decreaseLikes(itemId);
        }
        return false;
    }

    private String buildKey(Integer userId, LikeType likeType) {
        return RedisConstants.LIST_USER_LIKE + likeType.getType() + ":" + userId;
    }
}
