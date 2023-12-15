package top.wang3.hami.core.service.interact.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.zset.Tuple;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.message.interact.CollectRabbitMessage;
import top.wang3.hami.common.model.ArticleCollect;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RandomUtils;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.common.util.ZPageHandler;
import top.wang3.hami.core.annotation.CostLog;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.service.article.repository.ArticleRepository;
import top.wang3.hami.core.service.interact.CollectService;
import top.wang3.hami.core.service.interact.Interact;
import top.wang3.hami.core.service.interact.InteractHandler;
import top.wang3.hami.core.service.interact.repository.CollectRepository;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 文章收藏服务
 * todo 收藏夹支持
 */
@Service
@RequiredArgsConstructor
public class CollectServiceImplV2 implements CollectService {

    private final CollectRepository collectRepository;
    private final ArticleRepository articleRepository;
    private final RabbitMessagePublisher rabbitMessagePublisher;

    @Override
    public boolean doCollect(Integer itemId) {
        // todo 确保itemId存在
        int loginUserId = LoginUserContext.getLoginUserId();
        String key = buildKey(itemId);
        Interact<Integer> interact = Interact.ofDoAction(key, itemId);
        return InteractHandler.handleAction(interact, () -> {
            return loadCollectList(loginUserId);
        }, (act) -> {
            CollectRabbitMessage message = new CollectRabbitMessage(
                    loginUserId,
                    getItemUser(itemId),
                    Constants.ONE,
                    itemId
            );
            rabbitMessagePublisher.publishMsgSync(message);
        });
    }

    @Override
    public boolean cancelCollect(Integer itemId) {
        int loginUserId = LoginUserContext.getLoginUserId();
        String key = buildKey(itemId);
        Interact<Integer> interact = Interact.ofCancelAction(key, itemId);
        return InteractHandler.handleAction(interact, () -> {
            return loadCollectList(loginUserId);
        }, (act) -> {
            CollectRabbitMessage message = new CollectRabbitMessage(
                    loginUserId,
                    getItemUser(itemId),
                    Constants.ZERO,
                    itemId
            );
            rabbitMessagePublisher.publishMsgSync(message);
        });
    }

    @Override
    public boolean hasCollected(Integer userId, Integer itemId) {
        // 数据量小的情况可以直接存在Redis中
        Map<Integer, Boolean> map = hasCollected(userId, List.of(itemId));
        return map.getOrDefault(itemId, false);
    }

    @CostLog
    @Override
    public Map<Integer, Boolean> hasCollected(Integer userId, List<Integer> itemIds) {
        String key = buildKey(userId);
        if (getUserCollectCount(userId) == 0) {
            return Collections.emptyMap();
        }
        long timeout = TimeUnit.HOURS.toMillis(RandomUtils.randomLong(10, 100));
        boolean success = RedisClient.pExpire(key, timeout);
        if (!success) {
            synchronized (key.intern()) {
                success = RedisClient.pExpire(key, timeout);
                if (!success) {
                    loadUserCollects(key, userId, -1, -1);
                }
            }
        }
        return RedisClient.zMContains(key, itemIds);
    }

    @Override
    public Long getUserCollectCount(Integer userId) {
        //获取用户点赞的实体数 (我赞过)
        String key = RedisConstants.USER_COLLECT_COUNT + userId;
        long timeout = TimeUnit.HOURS.toMillis(RandomUtils.randomLong(1, 100));
        if (RedisClient.pExpire(key, timeout)) {
            return RedisClient.getCacheObject(key);
        } else {
            synchronized (key.intern()) {
                Long count = RedisClient.getCacheObject(key);
                if (count == null) {
                    count = collectRepository.getUserCollectCount(userId);
                    RedisClient.setCacheObject(key, count, timeout, TimeUnit.MILLISECONDS);
                }
                return count;
            }
        }
    }


    @Override
    public Collection<Integer> listUserCollects(Page<ArticleCollect> page, Integer userId) {
        String key = RedisConstants.USER_COLLECT_LIST + userId;
        return ZPageHandler
                .<Integer>of(key, page, key.intern())
                .countSupplier(() -> getUserCollectCount(userId))
                .loader((c, s) -> {
                    return loadUserCollects(key, userId, c, s);
                })
                .query();
    }


    @Override
    public Collection<Integer> loadUserCollects(String key, Integer userId, long current, long size) {
        List<ArticleCollect> collects = collectRepository.listUserCollects(userId);
        if (CollectionUtils.isEmpty(collects)) {
            return Collections.emptyList();
        }
        Collection<Tuple> tuples = ListMapperHandler.listToTuple(collects, ArticleCollect::getArticleId, (item) -> {
            return (double) item.getMtime().getTime();
        });
        RedisClient.zSetAll(key, tuples, RandomUtils.randomLong(10, 20), TimeUnit.DAYS);
        return ListMapperHandler.subList(collects, ArticleCollect::getArticleId, current, size);
    }

    private String buildKey(Integer userId) {
        return RedisConstants.USER_COLLECT_LIST + userId;
    }

    private Set<ZSetOperations.TypedTuple<Integer>> loadCollectList(int loginUserId) {
        List<ArticleCollect> likeItems = collectRepository.listUserCollects(loginUserId);
        return ListMapperHandler.listToZSet(likeItems, ArticleCollect::getArticleId, item -> {
            return (double) item.getMtime().getTime();
        });
    }

    private Integer getItemUser(Integer itemId) {
        return articleRepository.getArticleAuthor(itemId);
    }
}
