package top.wang3.hami.core.service.interact.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.zset.DefaultTuple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.message.CollectRabbitMessage;
import top.wang3.hami.common.model.ArticleCollect;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RandomUtils;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.annotation.CostLog;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.component.ZPageHandler;
import top.wang3.hami.core.exception.ServiceException;
import top.wang3.hami.core.service.article.repository.ArticleRepository;
import top.wang3.hami.core.service.article.repository.ArticleStatRepository;
import top.wang3.hami.core.service.interact.CollectService;
import top.wang3.hami.core.service.interact.repository.CollectRepository;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class CollectServiceImpl implements CollectService {

    private final CollectRepository collectRepository;
    private final RabbitMessagePublisher rabbitMessagePublisher;
    private final ArticleRepository articleRepository;
    private final ArticleStatRepository articleStatRepository;

    @Resource
    TransactionTemplate transactionTemplate;

    @Override
    public boolean doCollect(Integer itemId) {
        //用户收藏
        //文章收藏+1
        //发送收藏消息
        int loginUserId = LoginUserContext.getLoginUserId();
        Integer author = articleRepository.getArticleAuthor(itemId);
        if (author == null) {
            throw new ServiceException("参数错误");
        }
        Boolean success = transactionTemplate.execute(status -> {
            boolean success1 = collectRepository.doCollect(loginUserId, itemId);
            if (success1 && articleStatRepository.increaseCollects(itemId, 1)) {
                return true;
            }
            status.setRollbackOnly();
            return false;
        });
        if (!Boolean.TRUE.equals(success)) return false;
        CollectRabbitMessage message = new CollectRabbitMessage(loginUserId, author,
                Constants.ONE, itemId);
        rabbitMessagePublisher.publishMsg(message);
        return true;
    }

    @Override
    public boolean cancelCollect(Integer itemId) {
        int loginUserId = LoginUserContext.getLoginUserId();
        Integer author = articleRepository.getArticleAuthor(itemId);
        if (author == null) {
            throw new ServiceException("参数错误");
        }
        Boolean canceled = transactionTemplate.execute(status -> {
            boolean success = collectRepository.cancelCollect(loginUserId, itemId);
            if (success && articleStatRepository.decreaseCollects(itemId, 1)) {
                return true;
            }
            status.setRollbackOnly();
            return false;
        });
        if (!Boolean.TRUE.equals(canceled)) return false;
        CollectRabbitMessage message = new CollectRabbitMessage(loginUserId, author,
                Constants.ZERO, itemId);
        rabbitMessagePublisher.publishMsg(message);
        return true;
    }

    @Override
    public boolean hasCollected(Integer userId, Integer itemId) {
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
        boolean success = RedisClient.expire(key, RandomUtils.randomLong(10, 100), TimeUnit.HOURS);
        if (!success) {
            synchronized (this) {
                success = RedisClient.expire(key, RandomUtils.randomLong(10, 100), TimeUnit.HOURS);
                if (!success) {
                    loadUserCollects(key, userId, -1, -1);
                }
            }
        }
        return RedisClient.zMContains(key, itemIds);
    }

    @Override
    public Long getUserCollectCount(Integer userId) {
        String key = Constants.USER_COLLECT_COUNT + userId;
        Long count = RedisClient.getCacheObject(key);
        if (count != null) return count;
        synchronized (this) {
            count = RedisClient.getCacheObject(key);
            if (count == null) {
                count = collectRepository.getUserCollectCount(userId);
                RedisClient.setCacheObject(key, count, RandomUtils.randomLong(10, 20), TimeUnit.HOURS);
            }
            return count;
        }
    }


    @Override
    public Collection<Integer> listUserCollects(Page<ArticleCollect> page, Integer userId) {
        String key = Constants.LIST_USER_COLLECT + userId;
        return ZPageHandler
                .<Integer>of(key, page, this)
                .countSupplier(() -> getUserCollectCount(userId))
                .loader((c, s) -> {
                    return loadUserCollects(key, userId, c, s);
                })
                .query();
    }


    @Override
    public Collection<Integer> loadUserCollects(String key, Integer userId, long current, long size) {
        synchronized (this) {
            //todo 应该用分布式锁, 对userId进行加锁，避免锁粒度过大
            //全部查出来
            //用户收藏的文章一般不会太多
            List<ArticleCollect> collects = collectRepository.listUserCollects(userId);
            if (CollectionUtils.isEmpty(collects)) {
                return Collections.emptyList();
            }
            Collection<DefaultTuple> tuples = ListMapperHandler.listTo(collects, item -> {
                byte[] rawValue = RedisClient.valueBytes(item.getArticleId());
                Double score = (double) item.getMtime().getTime();
                return new DefaultTuple(rawValue, score);
            });
            RedisClient.zSetAll(key, tuples, RandomUtils.randomLong(10, 20), TimeUnit.DAYS);
            return ListMapperHandler.subList(collects, ArticleCollect::getArticleId, current, size);
        }
    }

    private String buildKey(Integer userId) {
        return Constants.LIST_USER_COLLECT + userId;
    }
}
