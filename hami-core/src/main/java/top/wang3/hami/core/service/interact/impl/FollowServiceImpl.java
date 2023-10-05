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
import top.wang3.hami.common.message.FollowRabbitMessage;
import top.wang3.hami.common.model.UserFollow;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RandomUtils;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.component.ZPageHandler;
import top.wang3.hami.core.exception.ServiceException;
import top.wang3.hami.core.repository.UserRepository;
import top.wang3.hami.core.service.interact.FollowService;
import top.wang3.hami.core.service.interact.repository.FollowRepository;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.Collections;
import java.util.HashMap;
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
        return followRepository.hasFollowed(userId, followingId);
    }

    @Override
    public Map<Integer, Boolean> hasFollowed(Integer userId, List<Integer> followingIds) {
        return followRepository.hasFollowed(userId, followingIds);
    }

    @Override
    public Map<Integer, Long> listUserFollowingCount(List<Integer> userIds) {
        List<String> keys = ListMapperHandler.listTo(userIds, id -> Constants.USER_FOLLOWING_COUNT + id);
        List<Long> counts = RedisClient.getMultiCacheObject(keys);
        HashMap<Integer, Long> result = new HashMap<>(userIds.size());
        ListMapperHandler.forEach(counts, (count, index) -> {
            Integer id = userIds.get(index);
            if (count == null) {
                count = getUserFollowingCount(id);
            }
            result.put(id, count);
        });
        return result;
    }

    @Override
    public Map<Integer, Long> listUserFollowerCount(List<Integer> userIds) {
        List<String> keys = ListMapperHandler.listTo(userIds, id -> Constants.USER_FOLLOWER_COUNT + id);
        List<Long> counts = RedisClient.getMultiCacheObject(keys);
        HashMap<Integer, Long> result = new HashMap<>(userIds.size());
        ListMapperHandler.forEach(counts, (count, index) -> {
            Integer id = userIds.get(index);
            if (count == null) {
                count = getUserFollowerCount(id);
            }
            result.put(id, count);
        });
        return result;
    }

    @Override
    public List<UserFollow> listUserFollowings(Integer userId) {
        return followRepository.listUserFollowings(userId);
    }

    @Override
    public List<UserFollow> listUserFollowers(Integer userId) {
        return followRepository.listUserFollowers(userId);
    }

    @Override
    public List<Integer> listUserFollowings(Page<UserFollow> page, int userId) {
        String key = Constants.LIST_USER_FOLLOWING + userId;
        return ZPageHandler.<Integer>of(key, page, this)
                .countSupplier(() -> getUserFollowingCount(userId))
                .source((current, size) -> {
                    page.setSearchCount(false);
                    return followRepository.listUserFollowings(page, userId);
                })
                .loader((current, size) -> {
                    return loadUserFollowings(key, userId, current, size);
                })
                .query();
    }

    @Override
    public List<Integer> listUserFollowers(Page<UserFollow> page, int userId) {
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
        if (Boolean.TRUE.equals(success)) {
            rabbitMessagePublisher.publishMsg(new FollowRabbitMessage(loginUserId, followingId, true));
            return true;
        }
        return false;
    }

    @Override
    public boolean unFollow(int followingId) {
        //用户取消关注
        //被关注用户的粉丝数-1
        //用户的关注数-1 (有Canal发送MQ消费)
        int loginUserId = LoginUserContext.getLoginUserId();
        Boolean success = transactionTemplate.execute(status -> {
            return followRepository.unFollow(loginUserId, followingId);
        });
        if (Boolean.TRUE.equals(success)) {
            rabbitMessagePublisher.publishMsg(new FollowRabbitMessage(loginUserId, followingId, false));
            return true;
        }
        return false;
    }

    private List<Integer> loadUserFollowings(String key, Integer userId, long current, long size) {
        List<UserFollow> follows = followRepository.listUserFollowings(userId);
        if (CollectionUtils.isEmpty(follows)) {
            return Collections.emptyList();
        }
        List<DefaultTuple> tuples = ListMapperHandler.listTo(follows, (item) -> {
            Double score = (double) item.getMtime().getTime();
            byte[] rawValue = RedisClient.valueBytes(item.getFollowing());
            return new DefaultTuple(rawValue, score);
        });
        RedisClient.zSetAll(key, tuples, RandomUtils.randomLong(10, 20), TimeUnit.DAYS);
        return ListMapperHandler.subList(follows, UserFollow::getFollowing, current, size);
    }

    private List<Integer> loadUserFollowers(String key, Integer userId, long current, long size) {
        List<UserFollow> followers = followRepository.listUserFollowers(userId);
        if (CollectionUtils.isEmpty(followers)) {
            return Collections.emptyList();
        }
        List<DefaultTuple> tuples = ListMapperHandler.listTo(followers, (item) -> {
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
}
