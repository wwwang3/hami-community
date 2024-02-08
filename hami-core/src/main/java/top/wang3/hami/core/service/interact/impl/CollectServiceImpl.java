package top.wang3.hami.core.service.interact.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.constant.TimeoutConstants;
import top.wang3.hami.common.model.ArticleCollect;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.annotation.CostLog;
import top.wang3.hami.core.cache.CacheService;
import top.wang3.hami.core.service.interact.CollectService;
import top.wang3.hami.core.service.interact.UserInteractService;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 文章收藏服务
 */
@Service
@RequiredArgsConstructor
public class CollectServiceImpl implements CollectService {

    private final CacheService cacheService;
    private final UserInteractService userInteractService;

    @Override
    public boolean doCollect(Integer itemId) {
        return userInteractService.collectAction(
                LoginUserContext.getLoginUserId(),
                itemId,
                true
        );
    }

    @Override
    public boolean cancelCollect(Integer itemId) {
        return userInteractService.collectAction(
                LoginUserContext.getLoginUserId(),
                itemId,
                false
        );
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
    public Integer getUserCollectCount(Integer userId) {
        //获取用户点赞的实体数 (我赞过)
        return userInteractService.getCollectCount(userId);
    }

    @Override
    public List<Integer> listUserCollects(Page<ArticleCollect> page, Integer userId) {
        return userInteractService.getCollectList(page, userId);
    }

    @Override
    public List<Integer> loadUserCollects(Integer userId) {
        return userInteractService.loadCollectList(userId);
    }

    private String buildKey(Integer userId) {
        return RedisConstants.USER_COLLECT_LIST + userId;
    }

}
