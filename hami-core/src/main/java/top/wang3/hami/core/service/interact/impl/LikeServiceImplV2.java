package top.wang3.hami.core.service.interact.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.zset.Tuple;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
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
import top.wang3.hami.core.service.article.repository.ArticleRepository;
import top.wang3.hami.core.service.comment.repository.CommentRepository;
import top.wang3.hami.core.service.interact.Interact;
import top.wang3.hami.core.service.interact.InteractHandler;
import top.wang3.hami.core.service.interact.LikeService;
import top.wang3.hami.core.service.interact.repository.LikeRepository;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class LikeServiceImplV2 implements LikeService {

    private final LikeRepository likeRepository;
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final RabbitMessagePublisher rabbitMessagePublisher;

    @Override
    public boolean doLike(Integer itemId, LikeType likeType) {
        // todo 确保itemId存在
        // 先写Redis 判断用户是否点赞和查询用户点赞列表都是从Redis读取
        // 当MySQL某个记录写入失败时且没有重试措施，会出现二者不一致问题
        int loginUserId = LoginUserContext.getLoginUserId();
        Interact<Integer> interact = Interact.ofDoAction(buildKey(loginUserId, likeType), itemId);
        return InteractHandler.handleAction(interact, () -> loadLikeList(loginUserId, likeType), act -> {
            // 执行成功的后置处理器. 发送MQ消息异步写入DB
            LikeRabbitMessage message = new LikeRabbitMessage(
                    loginUserId,
                    getItemUser(itemId, likeType), // maybe null
                    Constants.ONE,
                    itemId,
                    likeType
            );
            rabbitMessagePublisher.publishMsgSync(message);
        });
    }

    @Override
    public boolean cancelLike(Integer itemId, LikeType likeType) {
        int loginUserId = LoginUserContext.getLoginUserId();
        Interact<Integer> interact = Interact.ofCancelAction(buildKey(loginUserId, likeType), itemId);
        return InteractHandler.handleAction(interact, () -> {
            return loadLikeList(loginUserId, likeType);
        }, (act) -> {
            // 执行成功的后置处理器. 发送MQ消息异步写入DB
            LikeRabbitMessage message = new LikeRabbitMessage(
                    loginUserId,
                    getItemUser(itemId, likeType),
                    Constants.ZERO,
                    itemId,
                    likeType
            );
            rabbitMessagePublisher.publishMsgSync(message);
        });
    }

    private String buildKey(int userId, LikeType likeType) {
        return RedisConstants.LIST_USER_LIKE + likeType.getType() + ":" + userId;
    }

    private Set<ZSetOperations.TypedTuple<Integer>> loadLikeList(int loginUserId, LikeType likeType) {
        List<LikeItem> likeItems = likeRepository.listUserLikeItem(loginUserId, likeType);
        return ListMapperHandler.listToZSet(likeItems, LikeItem::getItemId, item -> {
            return (double) item.getMtime().getTime();
        });
    }

    private Integer getItemUser(Integer itemId, LikeType likeType) {
        Integer itemUser = null;
        if (LikeType.ARTICLE.equals(likeType)) {
            itemUser = articleRepository.getArticleAuthor(itemId);
        } else if (LikeType.COMMENT.equals(likeType)) {
            itemUser = commentRepository.getCommentUser(itemId);
        }
        return itemUser;
    }

    @CostLog
    @Override
    public Long getUserLikeCount(Integer userId, LikeType likeType) {
        //获取用户点赞的实体数 (我赞过)
        String key = RedisConstants.USER_LIKE_COUNT + likeType.getType() + ":" + userId;
        long timeout = TimeUnit.HOURS.toMillis(RandomUtils.randomLong(1, 100));
        if (RedisClient.pExpire(key, timeout)) {
            return RedisClient.getCacheObject(key);
        } else {
            synchronized (key.intern()) {
                Long count = RedisClient.getCacheObject(key);
                if (count == null) {
                    count = likeRepository.queryUserLikeItemCount(userId, likeType);
                    RedisClient.setCacheObject(key, count, timeout, TimeUnit.MILLISECONDS);
                }
                return count;
            }
        }
    }

    @CostLog
    @Override
    public Collection<Integer> listUserLikeArticles(Page<LikeItem> page, Integer userId) {
        //最近点赞的文章
        String key = buildKey(userId, LikeType.ARTICLE);
        return ZPageHandler.<Integer>of(key, page, key.intern())
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
        long timeout = TimeUnit.HOURS.toMillis(RandomUtils.randomLong(1, 100));
        boolean success = RedisClient.pExpire(key, timeout);
        if (!success) {
            synchronized (key.intern()) {
                success = RedisClient.pExpire(key, timeout);
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
        Collection<Tuple> tuples = ListMapperHandler.listToTuple(likeItems, LikeItem::getItemId, item -> {
            return (double) item.getMtime().getTime();
        });
        if (!tuples.isEmpty()) {
            RedisClient.zSetAll(key, tuples, RandomUtils.randomLong(1, 100), TimeUnit.HOURS);
        }
        return ListMapperHandler.subList(likeItems, LikeItem::getItemId, current, size);
    }
}
