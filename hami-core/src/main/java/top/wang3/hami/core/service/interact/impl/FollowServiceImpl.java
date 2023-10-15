package top.wang3.hami.core.service.interact.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.zset.DefaultTuple;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.message.FollowRabbitMessage;
import top.wang3.hami.common.model.UserFollow;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RandomUtils;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.annotation.CostLog;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.component.ZPageHandler;
import top.wang3.hami.core.exception.ServiceException;
import top.wang3.hami.core.service.interact.FollowService;
import top.wang3.hami.core.service.interact.repository.FollowRepository;
import top.wang3.hami.core.service.user.repository.UserRepository;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final RabbitMessagePublisher rabbitMessagePublisher;

    @Resource
    TransactionTemplate transactionTemplate;

    @NonNull
    @Override
    public Long getUserFollowingCount(Integer userId) {
        String key = Constants.USER_FOLLOWING_COUNT + userId;
        Long count = RedisClient.getCacheObject(key);
        if (count != null) return count;
        synchronized (this) {
            count = RedisClient.getCacheObject(key);
            if (count == null) {
                count = followRepository.getUserFollowingCount(userId);
                RedisClient.setCacheObject(key, count, RandomUtils.randomLong(10, 20), TimeUnit.DAYS);
            }
            return count;
        }
    }

    @NonNull
    @Override
    public Long getUserFollowerCount(Integer userId) {
        String key = Constants.USER_FOLLOWER_COUNT + userId;
        Long count = RedisClient.getCacheObject(key);
        if (count != null) return count;
        synchronized (this) {
            count = RedisClient.getCacheObject(key);
            if (count == null) {
                count = followRepository.getUserFollowerCount(userId);
                RedisClient.setCacheObject(key, count, RandomUtils.randomLong(10, 20), TimeUnit.DAYS);
            }
            return count;
        }
    }

    @Override
    public boolean hasFollowed(Integer userId, Integer followingId) {
        Map<Integer, Boolean> followed = hasFollowed(userId, List.of(followingId));
        return followed.getOrDefault(followingId, false);
    }

    @CostLog
    @Override
    public Map<Integer, Boolean> hasFollowed(Integer userId, List<Integer> followingIds) {
        String key = buildKey(userId);
        if (getUserFollowingCount(userId) == 0) {
            return Collections.emptyMap();
        }
        boolean success = RedisClient.expire(key, RandomUtils.randomLong(10, 100), TimeUnit.HOURS);
        if (!success) {
            synchronized (this) {
                success = RedisClient.expire(key, RandomUtils.randomLong(10, 100), TimeUnit.HOURS);
                if (!success) {
                    loadUserFollowings(key, userId, -1, -1);
                }
            }
        }
        return RedisClient.zMContains(key, followingIds);
    }

    @Override
    public Map<Integer, Long> listUserFollowingCount(List<Integer> userIds) {
        return RedisClient.getMultiCacheObjectToMap(Constants.USER_FOLLOWING_COUNT, userIds,
                followRepository::listUserFollowingCount);
    }

    @Override
    public Map<Integer, Long> listUserFollowerCount(List<Integer> userIds) {
        return RedisClient.getMultiCacheObjectToMap(Constants.USER_FOLLOWER_COUNT, userIds,
                followRepository::listUserFollowerCount);
    }

    @Override
    public Collection<Integer> listUserFollowings(Page<UserFollow> page, int userId) {
        String key = Constants.LIST_USER_FOLLOWING + userId;
        return ZPageHandler.<Integer>of(key, page, this)
                .countSupplier(() -> getUserFollowingCount(userId))
                .loader((current, size) -> {
                    return loadUserFollowings(key, userId, current, size);
                })
                .query();
    }

    @Override
    public Collection<Integer> listUserFollowers(Page<UserFollow> page, int userId) {
        String key = Constants.LIST_USER_FOLLOWER + userId;
        return ZPageHandler.<Integer>of(key, page, this)
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
    public boolean follow(int followingId) {
        //用户关注
        //被关注用户的粉丝数+1
        //用户的关注数+1 (有Canal发送MQ消费)
        //发送关注消息
        int loginUserId = LoginUserContext.getLoginUserId();
        //check user
        checkUser(loginUserId, followingId);
        //关注
        Boolean success = transactionTemplate.execute(status -> {
            return followRepository.follow(loginUserId, followingId);
        });
        if (!Boolean.TRUE.equals(success)) return false;
        FollowRabbitMessage message = new FollowRabbitMessage(loginUserId, followingId, Constants.ONE, null);
        rabbitMessagePublisher.publishMsg(message);
        return true;
    }

    @Override
    public boolean unFollow(int followingId) {
        //用户取消关注
        int loginUserId = LoginUserContext.getLoginUserId();
        Boolean success = transactionTemplate.execute(status -> {
            return followRepository.unFollow(loginUserId, followingId);
        });
        if (!Boolean.TRUE.equals(success)) return false;
        FollowRabbitMessage message = new FollowRabbitMessage(loginUserId, followingId, Constants.ZERO, null);
        rabbitMessagePublisher.publishMsg(message);
        return true;
    }

    @Override
    public List<Integer> loadUserFollowings(String key, Integer userId, long current, long size) {
        synchronized (this) {
            List<UserFollow> follows = followRepository.listUserFollowings(userId);
            if (CollectionUtils.isEmpty(follows)) {
                return Collections.emptyList();
            }
            Collection<DefaultTuple> tuples = ListMapperHandler.listTo(follows, (item) -> {
                Double score = (double) item.getMtime().getTime();
                byte[] rawValue = RedisClient.valueBytes(item.getFollowing());
                return new DefaultTuple(rawValue, score);
            });
            RedisClient.zSetAll(key, tuples, RandomUtils.randomLong(10, 20), TimeUnit.DAYS);
            return ListMapperHandler.subList(follows, UserFollow::getFollowing, current, size);
        }
    }

    @Override
    public List<Integer> loadUserFollowers(String key, Integer userId, long current, long size) {
        List<UserFollow> followers = followRepository.listUserFollowers(userId);
        if (CollectionUtils.isEmpty(followers)) {
            return Collections.emptyList();
        }
        Collection<DefaultTuple> tuples = ListMapperHandler.listTo(followers, (item) -> {
            Double score = (double) item.getMtime().getTime();
            byte[] rawValue = RedisClient.valueBytes(item.getUserId());
            return new DefaultTuple(rawValue, score);
        });
        RedisClient.zSetAll(key, tuples, RandomUtils.randomLong(10, 20), TimeUnit.DAYS);
        return ListMapperHandler.subList(followers, UserFollow::getFollowing, current, size);
    }

    private void checkUser(int userId, int following) {
        if (userId == following) {
            throw new ServiceException("自己不能关注自己");
        }
        if (!userRepository.checkUserExist(following)) {
            throw new ServiceException("用户不存在");
        }
    }

    private String buildKey(Integer userId) {
        return Constants.LIST_USER_FOLLOWING + userId;
    }
}
