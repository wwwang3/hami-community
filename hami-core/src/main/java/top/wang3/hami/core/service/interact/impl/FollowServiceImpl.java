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
import top.wang3.hami.common.constant.TimeoutConstants;
import top.wang3.hami.common.lock.LockTemplate;
import top.wang3.hami.common.message.interact.FollowRabbitMessage;
import top.wang3.hami.common.model.UserFollow;
import top.wang3.hami.common.util.InteractHandler;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.common.util.ZPageHandler;
import top.wang3.hami.core.cache.CacheService;
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
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final RabbitMessagePublisher rabbitMessagePublisher;
    private final CacheService cacheService;
    private final LockTemplate lockTemplate;

    @Override
    public boolean follow(int followingId) {
        final int loginUserId = LoginUserContext.getLoginUserId();
        String key = RedisConstants.USER_FOLLOWING_LIST + loginUserId;
        return InteractHandler
                .<Integer>build("关注")
                .ofAction(key, followingId)
                .millis(TimeoutConstants.FOLLOWING_LIST_EXPIRE)
                .loader(() -> loadUserFollowings(loginUserId))
                .postAct(() -> {
                    FollowRabbitMessage message = new FollowRabbitMessage(
                            loginUserId,
                            followingId,
                            Constants.ONE,
                            null);
                    rabbitMessagePublisher.publishMsgSync(message);
                })
                .execute();
    }

    @Override
    public boolean unFollow(int followingId) {
        final int loginUserId = LoginUserContext.getLoginUserId();
        String key = RedisConstants.USER_FOLLOWING_LIST + loginUserId;
        return InteractHandler
                .<Integer>build("取消关注")
                .ofCancelAction(key, followingId)
                .millis(TimeoutConstants.FOLLOWER_LIST_EXPIRE)
                .loader(() -> loadUserFollowings(loginUserId))
                .postAct(() -> {
                    // 发布关注事件
                    FollowRabbitMessage message = new FollowRabbitMessage(
                            loginUserId,
                            followingId,
                            Constants.ZERO,
                            null);
                    rabbitMessagePublisher.publishMsgSync(message);
                })
                .execute();
    }

    @Override
    @NonNull
    public Long getUserFollowingCount(Integer userId) {
        String key = RedisConstants.USER_FOLLOWING_COUNT + userId;
        return cacheService.get(
                key,
                () -> followRepository.getUserFollowingCount(userId),
                TimeoutConstants.INTERACT_COUNT_EXPIRE,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    @NonNull
    public Long getUserFollowerCount(Integer userId) {
        String key = RedisConstants.USER_FOLLOWER_COUNT + userId;
        return cacheService.get(
                key,
                () -> followRepository.getUserFollowerCount(userId),
                TimeoutConstants.INTERACT_COUNT_EXPIRE,
                TimeUnit.MILLISECONDS
        );
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
        cacheService.expiredThenExecute(key, () -> loadUserFollowings(userId), timeout);
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
    public List<Integer> listUserFollowings(Page<UserFollow> page, int userId) {
        String key = RedisConstants.USER_FOLLOWING_LIST + userId;
        return ZPageHandler.<Integer>of(key, page)
                .countSupplier(() -> getUserFollowingCount(userId))
                .loader(() -> loadUserFollowings(userId))
                .query();
    }

    @Override
    public List<Integer> listUserFollowers(Page<UserFollow> page, int userId) {
        String key = RedisConstants.USER_FOLLOWER_LIST + userId;
        return ZPageHandler.<Integer>of(key, page)
                .countSupplier(() -> getUserFollowerCount(userId))
                .source((current, size) -> {
                    page.setSearchCount(false);
                    return followRepository.listUserFollowers(page, userId);
                })
                .loader(() -> loadUserFollowers(userId))
                .query();
    }

    @Override
    public List<Integer> loadUserFollowings(Integer userId) {
        String key = RedisConstants.USER_FOLLOWING_LIST + userId;
        List<UserFollow> follows = followRepository.listUserFollowings(userId);
        if (CollectionUtils.isEmpty(follows)) {
            return Collections.emptyList();
        }
        Collection<Tuple> tuples = ListMapperHandler.listToTuple(
                follows,
                UserFollow::getFollowing,
                item -> item.getMtime().getTime()
        );
        RedisClient.zSetAll(
                key,
                tuples,
                TimeoutConstants.FOLLOWING_LIST_EXPIRE,
                TimeUnit.MILLISECONDS
        );
        return ListMapperHandler.listTo(follows, UserFollow::getFollowing, false);
    }

    @Override
    public List<Integer> loadUserFollowers(Integer userId) {
        String key = RedisConstants.USER_FOLLOWER_LIST + userId;
        List<UserFollow> followers = followRepository.listUserFollowers(userId);
        if (CollectionUtils.isEmpty(followers)) {
            return Collections.emptyList();
        }
        Collection<Tuple> tuples = ListMapperHandler.listToTuple(
                followers,
                UserFollow::getFollowing,
                item -> item.getMtime().getTime()
        );
        RedisClient.zSetAll(
                key,
                tuples,
                TimeoutConstants.FOLLOWER_LIST_EXPIRE,
                TimeUnit.MILLISECONDS
        );
        return ListMapperHandler.listTo(followers, UserFollow::getUserId, false);
    }

}
