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
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.component.ZPageHandler;
import top.wang3.hami.core.exception.ServiceException;
import top.wang3.hami.core.repository.ArticleRepository;
import top.wang3.hami.core.repository.ArticleStatRepository;
import top.wang3.hami.core.service.interact.CollectService;
import top.wang3.hami.core.service.interact.repository.CollectRepository;
import top.wang3.hami.security.context.LoginUserContext;

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
    public boolean doCollect(Integer userId, Integer itemId) {
        //用户收藏
        //文章收藏+1
        //发送收藏消息
        int loginUserId = LoginUserContext.getLoginUserId();
        if (!articleRepository.checkArticleExist(itemId)) {
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
        if (Boolean.TRUE.equals(success)) {
            rabbitMessagePublisher.publishMsg(new CollectRabbitMessage(loginUserId, itemId, true));
            return true;
        }
        return false;
    }

    @Override
    public boolean cancelCollect(Integer userId, Integer itemId) {
        int loginUserId = LoginUserContext.getLoginUserId();
        Boolean canceled = transactionTemplate.execute(status -> {
            boolean success = collectRepository.cancelCollect(loginUserId, itemId);
            if (success && articleStatRepository.decreaseCollects(itemId, 1)) {
                return true;
            }
            status.setRollbackOnly();
            return false;
        });
        if (Boolean.TRUE.equals(canceled)) {
            rabbitMessagePublisher.publishMsg(new CollectRabbitMessage(loginUserId, itemId, false));
            return false;
        }
        return true;
    }

    @Override
    public boolean hasCollected(Integer userId, Integer itemId) {
        return collectRepository.hasCollected(userId, itemId);
    }

    @Override
    public Map<Integer, Boolean> hasCollected(Integer userId, List<Integer> itemIds) {
        return collectRepository.hasCollected(userId, itemIds);
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
    public List<ArticleCollect> listUserCollects(Integer userId, int max) {
        return collectRepository.listUserCollects(userId, max);
    }

    @Override
    public List<Integer> listUserCollects(Page<ArticleCollect> page, Integer userId) {
        String key = Constants.LIST_USER_COLLECT + userId;
        return ZPageHandler
                .<Integer>of(key, page, this)
                .countSupplier(() -> getUserCollectCount(userId))
                .source((current, size) -> {
                    return collectRepository.listUserCollects(page, userId);
                })
                .loader((c, s) -> {
                    return loadUSerCollects(key, userId, c, s);
                })
                .query();
    }


    public List<Integer> loadUSerCollects(String key, Integer userId, long current, long size) {
        List<ArticleCollect> collects = listUserCollects(userId, ZPageHandler.DEFAULT_MAX_SIZE);
        if (CollectionUtils.isEmpty(collects)) {
            return Collections.emptyList();
        }
        List<DefaultTuple> tuples = ListMapperHandler.listTo(collects, item -> {
            byte[] rawValue = RedisClient.valueBytes(item.getArticleId());
            Double score = (double) item.getMtime().getTime();
            return new DefaultTuple(rawValue, score);
        });
        RedisClient.zSetAll(key, tuples, RandomUtils.randomLong(10, 20), TimeUnit.DAYS);
        return ListMapperHandler.subList(collects, ArticleCollect::getArticleId, current, size);
    }
}
