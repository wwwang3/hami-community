package top.wang3.hami.core.service.interact.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.constant.TimeoutConstants;
import top.wang3.hami.common.dto.interact.LikeType;
import top.wang3.hami.common.model.LikeItem;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.annotation.CostLog;
import top.wang3.hami.core.cache.CacheService;
import top.wang3.hami.core.service.interact.LikeService;
import top.wang3.hami.core.service.interact.UserInteractService;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final UserInteractService userInteractService;
    private final CacheService cacheService;

    @Override
    public boolean doLike(Integer itemId, LikeType likeType) {
        return userInteractService.likeAction(
                LoginUserContext.getLoginUserId(),
                itemId,
                likeType,
                true
        );
    }

    @Override
    public boolean cancelLike(Integer itemId, LikeType likeType) {
        return userInteractService.likeAction(
                LoginUserContext.getLoginUserId(),
                itemId,
                likeType,
                false
        );
    }

    @CostLog
    @Override
    public Integer getUserLikeCount(Integer userId, LikeType likeType) {
        // 获取用户点赞的实体数 (我赞过)
        return userInteractService.getLikeCount(userId, likeType);
    }

    @CostLog
    @Override
    public List<Integer> listUserLikeArticles(Page<LikeItem> page, Integer userId) {
        // 最近点赞的文章
        return userInteractService.getLikeList(page, userId, LikeType.ARTICLE);
    }

    @Override
    public boolean hasLiked(Integer userId, Integer itemId, LikeType likeType) {
        // 两次redis操作
        // 感觉可以用hash结构存储ttl和点赞的item
        // 判断ttl过期则重新加载 减少一次io
        Map<Integer, Boolean> liked = hasLiked(userId, List.of(itemId), likeType);
        return liked.getOrDefault(itemId, false);
    }

    @CostLog
    @Override
    public Map<Integer, Boolean> hasLiked(Integer userId, List<Integer> itemIds, LikeType likeType) {
        String key = RedisConstants.USER_LIKE_LIST + likeType.getType() + ":" + userId;
        if (getUserLikeCount(userId, likeType) == 0) {
            return Collections.emptyMap();
        }
        cacheService.expiredThenExecute(
                key,
                TimeoutConstants.LIKE_LIST_EXPIRE,
                () -> loadUserLikeItem(userId, likeType)
        );
        return RedisClient.zMContains(key, itemIds);
    }

    @Override
    public List<Integer> loadUserLikeItem(Integer userId, LikeType likeType) {
        return userInteractService.loadLikeList(userId, likeType);
    }

}
