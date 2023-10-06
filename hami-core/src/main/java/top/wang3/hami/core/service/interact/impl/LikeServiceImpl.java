package top.wang3.hami.core.service.interact.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.enums.LikeType;
import top.wang3.hami.common.message.LikeRabbitMessage;
import top.wang3.hami.common.model.LikeItem;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RandomUtils;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.annotation.CostLog;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.component.ZPageHandler;
import top.wang3.hami.core.exception.ServiceException;
import top.wang3.hami.core.service.article.repository.ArticleRepository;
import top.wang3.hami.core.service.article.repository.ArticleStatRepository;
import top.wang3.hami.core.service.comment.repository.CommentRepository;
import top.wang3.hami.core.service.interact.LikeService;
import top.wang3.hami.core.service.interact.repository.LikeRepository;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final ArticleStatRepository articleStatRepository;
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final RabbitMessagePublisher rabbitMessagePublisher;

    @Resource
    TransactionTemplate transactionTemplate;

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
            if (success1 && reduceLikeCount(loginUserId, likeType)) {
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
        String key = Constants.USER_LIKE_COUNT + likeType.getType() + ":" + userId;
        Long count = RedisClient.getCacheObject(Constants.USER_LIKE_COUNT);
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
    public List<Integer> listUserLikeArticles(Page<LikeItem> page, Integer userId) {
        //最近点赞的文章
        String key = Constants.LIST_USER_LIKE_ARTICLES + userId;
        return ZPageHandler.<Integer>of(key, page, this) //不同业务不同的锁
                .countSupplier(() -> {
                    return getUserLikeCount(userId, LikeType.ARTICLE);
                })
                .source((current, size) -> {
                    //回源查询
                    Page<LikeItem> itemPage = new Page<>(current, size, false);
                    return likeRepository.listUserLikeItem(itemPage, userId, LikeType.ARTICLE);
                })
                .loader((c, s) -> {
                    return loadUserLikeArticleCache(key, userId, c, s);
                })
                .query();
    }

    @Override
    public boolean hasLiked(Integer userId, Integer itemId, LikeType likeType) {
        return likeRepository.hasLiked(userId, itemId, likeType);
    }

    @Override
    public Map<Integer, Boolean> hasLiked(Integer userId, List<Integer> itemId, LikeType likeType) {
        return likeRepository.hasLiked(userId, itemId, likeType);
    }

    @Override
    public List<Integer> loadUserLikeArticleCache(String key, Integer userId, long current, long size) {
        //md 加锁 缓存刚好过期时, 有大量的请求过来, 性能应该会比较低
        List<LikeItem> likeItems = likeRepository.listUserLikeItem(userId, LikeType.ARTICLE);
        var tuples = ListMapperHandler.listToZSet(likeItems, LikeItem::getItemId, item -> {
            return (double) item.getMtime().getTime();
        });
        if (!tuples.isEmpty()) {
            RedisClient.deleteObject(key);
            RedisClient.zAddAll(key, tuples);
            RedisClient.expire(key, RandomUtils.randomLong(10L, 15L), TimeUnit.DAYS);
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
        if (itemUser == null) throw new ServiceException("参数错误");
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
}
