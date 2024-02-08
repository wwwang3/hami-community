package top.wang3.hami.core.service.interact.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.constant.TimeoutConstants;
import top.wang3.hami.common.model.UserFollow;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.cache.CacheService;
import top.wang3.hami.core.service.interact.FollowService;
import top.wang3.hami.core.service.interact.UserInteractService;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
@Primary
public class FollowServiceImpl implements FollowService {

    private final UserInteractService userInteractService;
    private final CacheService cacheService;

    @Override
    public boolean follow(int followingId) {
        return userInteractService.followAction(
                LoginUserContext.getLoginUserId(),
                followingId,
                true
        );
    }

    @Override
    public boolean unFollow(int followingId) {
        return userInteractService.followAction(
                LoginUserContext.getLoginUserId(),
                followingId,
                false
        );
    }

    @Override
    @NonNull
    public Integer getUserFollowingCount(Integer userId) {
        return userInteractService.getFollowCount(userId);
    }

    @Override
    @NonNull
    public Integer getUserFollowerCount(Integer userId) {
        return userInteractService.getFollowerCount(userId);
    }

    @Override
    public boolean hasFollowed(Integer userId, Integer followingId) {
        Map<Integer, Boolean> followed = hasFollowed(userId, List.of(followingId));
        return followed.getOrDefault(followingId, false);
    }

    @Override
    public Map<Integer, Boolean> hasFollowed(Integer userId, List<Integer> followingIds) {
        String key = RedisConstants.USER_FOLLOWING_LIST + userId;
        if (getUserFollowingCount(userId) == 0) {
            return Collections.emptyMap();
        }
        long timeout = TimeoutConstants.FOLLOWING_LIST_EXPIRE;
        cacheService.expiredThenExecute(key, timeout, () -> loadUserFollowings(userId));
        return RedisClient.zMContains(key, followingIds);
    }

    @Override
    public List<Integer> listUserFollowings(Page<UserFollow> page, int userId) {
        return userInteractService.getFollowList(page, userId);
    }

    @Override
    public List<Integer> listUserFollowers(Page<UserFollow> page, int userId) {
       return userInteractService.getFollowerList(page, userId);
    }

    @Override
    public List<Integer> loadUserFollowings(Integer userId) {
        return userInteractService.loadFollowList(userId);
    }

    @Override
    public List<Integer> loadUserFollowers(Integer userId) {
        return userInteractService.loadFollowerList(userId);
    }

}
