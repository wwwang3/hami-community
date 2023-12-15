package top.wang3.hami.core.service.interact.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.zset.Tuple;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.message.interact.FollowRabbitMessage;
import top.wang3.hami.common.model.UserFollow;
import top.wang3.hami.common.util.*;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.service.interact.FollowService;
import top.wang3.hami.core.service.interact.repository.FollowRepository;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
@Slf4j
@Primary
public class FollowServiceImplV2 implements FollowService {

    private final FollowRepository followRepository;
    private final RabbitMessagePublisher rabbitMessagePublisher;

    @Override
    public boolean follow(int followingId) {
        final int loginUserId = LoginUserContext.getLoginUserId();
        String key = RedisConstants.USER_FOLLOWING_LIST + loginUserId;
        return InteractHandler
                .<Integer>of("关注")
                .ofAction(key, followingId)
                .preCheck(i -> {
                    // todo 检查用户ID是否存在
                })
                .loader(() -> loadUserFollowings(loginUserId))
                .postAct(() -> {
                    FollowRabbitMessage message = new FollowRabbitMessage(
                            loginUserId,
                            followingId,
                            Constants.ONE,
                            null);
                    rabbitMessagePublisher.publishMsg(message);
                })
                .execute();
    }

    @Override
    public boolean unFollow(int followingId) {
        final int loginUserId = LoginUserContext.getLoginUserId();
        String key = RedisConstants.USER_FOLLOWING_LIST + loginUserId;
        return InteractHandler
                .<Integer>of("关注")
                .ofCancelAction(key, followingId)
                .preCheck(i -> {
                    // todo 检查用户ID是否存在
                })
                .loader(() -> loadUserFollowings(loginUserId))
                .postAct(() -> {
                    FollowRabbitMessage message = new FollowRabbitMessage(
                            loginUserId,
                            followingId,
                            Constants.ZERO,
                            null);
                    rabbitMessagePublisher.publishMsg(message);
                })
                .execute();
    }

    @Override
    @NonNull
    public Long getUserFollowingCount(Integer userId) {
        String key = RedisConstants.USER_FOLLOWING_COUNT + userId;
        Long count = RedisClient.getCacheObject(key);
        if (count != null) return count;
        synchronized (key.intern()) {
            count = RedisClient.getCacheObject(key);
            if (count == null) {
                count = followRepository.getUserFollowingCount(userId);
                RedisClient.setCacheObject(key, count, RandomUtils.randomLong(1, 20), TimeUnit.HOURS);
            }
            return count;
        }
    }

    @Override
    @NonNull
    public Long getUserFollowerCount(Integer userId) {
        String key = RedisConstants.USER_FOLLOWER_COUNT + userId;
        Long count = RedisClient.getCacheObject(key);
        if (count != null) return count;
        synchronized (key.intern()) {
            count = RedisClient.getCacheObject(key);
            if (count == null) {
                count = followRepository.getUserFollowerCount(userId);
                RedisClient.setCacheObject(key, count, RandomUtils.randomLong(1, 20), TimeUnit.HOURS);
            }
            return count;
        }
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
        long timeout = TimeUnit.HOURS.toMillis(RandomUtils.randomLong(10, 100));
        boolean success = RedisClient.pExpire(key, timeout);
        if (!success) {
            synchronized (key.intern()) {
                success = RedisClient.pExpire(key, timeout);
                if (!success) {
                    loadUserFollowings(key, userId, -1, -1);
                }
            }
        }
        return RedisClient.zMContains(key, followingIds);
    }

    @Override
    public Map<Integer, Long> listUserFollowingCount(List<Integer> userIds) {
        return RedisClient.getMultiCacheObjectToMap(RedisConstants.USER_FOLLOWING_COUNT, userIds,
                followRepository::listUserFollowingCount);
    }

    @Override
    public Map<Integer, Long> listUserFollowerCount(List<Integer> userIds) {
        return RedisClient.getMultiCacheObjectToMap(RedisConstants.USER_FOLLOWER_COUNT, userIds,
                followRepository::listUserFollowerCount);
    }

    @Override
    public Collection<Integer> listUserFollowings(Page<UserFollow> page, int userId) {
        String key = RedisConstants.USER_FOLLOWING_LIST + userId;
        return ZPageHandler.<Integer>of(key, page)
                .countSupplier(() -> getUserFollowingCount(userId))
                .loader((current, size) -> {
                    return loadUserFollowings(key, userId, current, size);
                })
                .query();
    }

    @Override
    public Collection<Integer> listUserFollowers(Page<UserFollow> page, int userId) {
        String key = RedisConstants.USER_FOLLOWER_LIST + userId;
        return ZPageHandler.<Integer>of(key, page)
                .countSupplier(() -> getUserFollowerCount(userId))
                .source((current, size) -> {
                    page.setSearchCount(false);
                    return followRepository.listUserFollowers(page, userId);
                })
                .loader((current, size) -> {
                    return loadUserFollowers(key, userId, current, size);
                })
                .query();
    }

    @Override
    public List<Integer> loadUserFollowings(String key, Integer userId, long current, long size) {
        List<UserFollow> follows = followRepository.listUserFollowings(userId);
        if (CollectionUtils.isEmpty(follows)) {
            return Collections.emptyList();
        }
        Collection<Tuple> tuples = ListMapperHandler.listToTuple(follows, UserFollow::getFollowing, item -> {
            return item.getMtime().getTime();
        });
        setCache(key, tuples, RandomUtils.randomLong(1, 100));
        return ListMapperHandler.subList(follows, UserFollow::getFollowing, current, size);
    }

    public Collection<Tuple> loadUserFollowings(Integer userId) {
        List<UserFollow> follows = followRepository.listUserFollowings(userId);
        if (CollectionUtils.isEmpty(follows)) {
            return Collections.emptyList();
        }
        return ListMapperHandler.listToTuple(follows, UserFollow::getFollowing, item -> {
            return item.getMtime().getTime();
        });
    }

    @Override
    public List<Integer> loadUserFollowers(String key, Integer userId, long current, long size) {
        List<UserFollow> followers = followRepository.listUserFollowers(userId);
        if (CollectionUtils.isEmpty(followers)) {
            return Collections.emptyList();
        }
        Collection<Tuple> tuples = ListMapperHandler.listToTuple(followers, UserFollow::getUserId, item -> {
            return item.getMtime().getTime();
        });
        setCache(key, tuples, RandomUtils.randomLong(1, 50));
        return ListMapperHandler.subList(followers, UserFollow::getUserId, current, size);
    }

    private void setCache(String key, Collection<Tuple> items, long timeout) {
        RedisClient.zSetAll(key, items, timeout, TimeUnit.HOURS);

    }
}
