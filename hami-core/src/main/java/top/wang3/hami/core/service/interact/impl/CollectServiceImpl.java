package top.wang3.hami.core.service.interact.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.zset.Tuple;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.constant.TimeoutConstants;
import top.wang3.hami.common.message.interact.CollectRabbitMessage;
import top.wang3.hami.common.model.ArticleCollect;
import top.wang3.hami.common.util.InteractHandler;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.common.util.ZPageHandler;
import top.wang3.hami.core.annotation.CostLog;
import top.wang3.hami.core.cache.CacheService;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.service.article.repository.ArticleRepository;
import top.wang3.hami.core.service.interact.CollectService;
import top.wang3.hami.core.service.interact.repository.CollectRepository;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 文章收藏服务
 * todo 收藏夹支持
 */
@Service
@RequiredArgsConstructor
public class CollectServiceImpl implements CollectService {

    private final CollectRepository collectRepository;
    private final ArticleRepository articleRepository;
    private final RabbitMessagePublisher rabbitMessagePublisher;
    private final CacheService cacheService;

    @Override
    public boolean doCollect(Integer itemId) {
        // todo 确保itemId存在
        int loginUserId = LoginUserContext.getLoginUserId();
        String key = RedisConstants.USER_COLLECT_LIST + loginUserId;
        return InteractHandler
                .of("收藏")
                .ofAction(key, itemId)
                .timeout(TimeoutConstants.COLLECT_LIST_EXPIRE, TimeUnit.MILLISECONDS)
                .loader(() -> loadCollectList(loginUserId))
                .postAct(() -> {
                    CollectRabbitMessage message = new CollectRabbitMessage(
                            loginUserId,
                            getItemUser(itemId),
                            Constants.ONE,
                            itemId
                    );
                    rabbitMessagePublisher.publishMsg(message);
                })
                .execute();
    }

    @Override
    public boolean cancelCollect(Integer itemId) {
        int loginUserId = LoginUserContext.getLoginUserId();
        String key = RedisConstants.USER_COLLECT_LIST + loginUserId;
        return InteractHandler
                .of("取消收藏")
                .ofCancelAction(key, itemId)
                .timeout(TimeoutConstants.COLLECT_LIST_EXPIRE, TimeUnit.MILLISECONDS)
                .loader(() -> loadCollectList(loginUserId))
                .postAct(() -> {
                    CollectRabbitMessage message = new CollectRabbitMessage(
                            loginUserId,
                            getItemUser(itemId),
                            Constants.ZERO,
                            itemId
                    );
                    rabbitMessagePublisher.publishMsg(message);
                })
                .execute();
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
        long timeout = TimeoutConstants.COLLECT_LIST_EXPIRE;
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
        return cacheService.get(
                key,
                () -> collectRepository.getUserCollectCount(userId),
                TimeoutConstants.INTERACT_COUNT_EXPIRE,
                TimeUnit.MILLISECONDS
        );
    }


    @Override
    public Collection<Integer> listUserCollects(Page<ArticleCollect> page, Integer userId) {
        String key = RedisConstants.USER_COLLECT_LIST + userId;
        return ZPageHandler
                .<Integer>of(key, page)
                .countSupplier(() -> getUserCollectCount(userId))
                .loader((c, s) -> loadUserCollects(key, userId, c, s))
                .query();
    }

    @Override
    public Collection<Integer> loadUserCollects(String key, Integer userId, long current, long size) {
        List<ArticleCollect> collects = collectRepository.listUserCollects(userId);
        if (CollectionUtils.isEmpty(collects)) {
            return Collections.emptyList();
        }
        Collection<Tuple> tuples = ListMapperHandler.listToTuple(
                collects,
                ArticleCollect::getArticleId,
                (item) -> (double) item.getMtime().getTime()
        );
        RedisClient.zSetAll(key, tuples, TimeoutConstants.COLLECT_LIST_EXPIRE, TimeUnit.MILLISECONDS);
        return ListMapperHandler.subList(collects, ArticleCollect::getArticleId, current, size);
    }

    private String buildKey(Integer userId) {
        return RedisConstants.USER_COLLECT_LIST + userId;
    }

    private Collection<Tuple> loadCollectList(Integer userId) {
        List<ArticleCollect> collects = collectRepository.listUserCollects(userId);
        if (CollectionUtils.isEmpty(collects)) {
            return Collections.emptyList();
        }
        return ListMapperHandler.listToTuple(
                collects,
                ArticleCollect::getArticleId,
                (item) -> (double) item.getMtime().getTime()
        );
    }

    private Integer getItemUser(Integer itemId) {
        return articleRepository.getArticleAuthor(itemId);
    }
}
