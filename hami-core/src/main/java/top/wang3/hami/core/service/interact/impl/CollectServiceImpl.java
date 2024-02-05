package top.wang3.hami.core.service.interact.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.zset.Tuple;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.constant.TimeoutConstants;
import top.wang3.hami.common.lock.LockTemplate;
import top.wang3.hami.common.message.interact.CollectRabbitMessage;
import top.wang3.hami.common.model.ArticleCollect;
import top.wang3.hami.common.util.InteractHandler;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.common.util.ZPageHandler;
import top.wang3.hami.core.annotation.CostLog;
import top.wang3.hami.core.cache.CacheService;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.exception.HamiServiceException;
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
 */
@Service
@RequiredArgsConstructor
public class CollectServiceImpl implements CollectService {

    private final CollectRepository collectRepository;
    private final ArticleRepository articleRepository;
    private final RabbitMessagePublisher rabbitMessagePublisher;
    private final CacheService cacheService;
    private final LockTemplate lockTemplate;

    @Override
    public boolean doCollect(Integer itemId) {
        // todo 确保itemId存在
        int loginUserId = LoginUserContext.getLoginUserId();
        String key = RedisConstants.USER_COLLECT_LIST + loginUserId;
        return InteractHandler
                .<Integer>build("收藏")
                .ofAction(key, itemId)
                .millis(TimeoutConstants.COLLECT_LIST_EXPIRE)
                .preCheck(member -> {
                    if (hasCollected(loginUserId, itemId)) throw new HamiServiceException("重复收藏");
                })
                .loader(() -> loadCollectList(loginUserId))
                .postAct(() -> {
                    CollectRabbitMessage message = new CollectRabbitMessage(
                            loginUserId,
                            getItemUser(itemId),
                            Constants.ONE,
                            itemId
                    );
                    rabbitMessagePublisher.publishMsgSync(message);
                })
                .execute();
    }

    @Override
    public boolean cancelCollect(Integer itemId) {
        int loginUserId = LoginUserContext.getLoginUserId();
        String key = RedisConstants.USER_COLLECT_LIST + loginUserId;
        return InteractHandler
                .<Integer>build("取消收藏")
                .ofCancelAction(key, itemId)
                .millis(TimeoutConstants.COLLECT_LIST_EXPIRE)
                .preCheck(member -> {
                    if (!hasCollected(loginUserId, member)) {
                        // 没有收藏执行取消收藏
                        throw new HamiServiceException("没有收藏该文章");
                    }
                })
                .loader(() -> loadUserCollects(loginUserId))
                .postAct(() -> {
                    CollectRabbitMessage message = new CollectRabbitMessage(
                            loginUserId,
                            getItemUser(itemId),
                            Constants.ZERO,
                            itemId
                    );
                    rabbitMessagePublisher.publishMsgSync(message);
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
        cacheService.expiredThenExecute(key, timeout, () -> loadUserCollects(userId));
        return RedisClient.zMContains(key, itemIds);
    }

    @Override
    public Long getUserCollectCount(Integer userId) {
        //获取用户点赞的实体数 (我赞过)
        String key = RedisConstants.USER_COLLECT_COUNT + userId;
        return cacheService.get(
                key,
                () -> collectRepository.getUserCollectCount(userId),
                TimeoutConstants.INTERACT_COUNT_EXPIRE
        );
    }


    @Override
    public List<Integer> listUserCollects(Page<ArticleCollect> page, Integer userId) {
        String key = buildKey(userId);
        return ZPageHandler
                .<Integer>of(key, page)
                .countSupplier(() -> getUserCollectCount(userId))
                .loader(() -> loadUserCollects(userId))
                .query();
    }

    @Override
    public List<Integer> loadUserCollects(Integer userId) {
        String key = buildKey(userId);
        List<ArticleCollect> collects = collectRepository.listUserCollects(userId);
        if (CollectionUtils.isEmpty(collects)) {
            return Collections.emptyList();
        }
        Collection<Tuple> tuples = ListMapperHandler.listToTuple(
                collects,
                ArticleCollect::getArticleId,
                (item) -> (double) item.getMtime().getTime()
        );
        RedisClient.zSetAll(
                key,
                tuples,
                TimeoutConstants.COLLECT_LIST_EXPIRE,
                TimeUnit.MILLISECONDS
        );
        return ListMapperHandler.listTo(collects, ArticleCollect::getArticleId, false);
    }

    private String buildKey(Integer userId) {
        return RedisConstants.USER_COLLECT_LIST + userId;
    }

    private void loadCollectList(Integer userId) {
        lockTemplate.execute(RedisConstants.USER_COLLECT_LIST + userId, () -> loadCollectList(userId));
    }

    private Integer getItemUser(Integer itemId) {
        return articleRepository.getArticleAuthor(itemId);
    }
}
