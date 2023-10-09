package top.wang3.hami.core.service.interact.handler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.annotation.CanalListener;
import top.wang3.hami.common.canal.CanalEntryHandler;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.model.UserFollow;
import top.wang3.hami.common.util.RandomUtils;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.component.ZPageHandler;
import top.wang3.hami.core.service.interact.FollowService;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 用户关注
 */
@Component
@CanalListener(value = "user_follow")
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings(value = "rawtypes")
public class FollowCanalHandler implements CanalEntryHandler<UserFollow> {


    private final FollowService followService;

    private RedisScript<Long> followingScript;
    private RedisScript<Long> followerScript;

    @PostConstruct
    public void init() {
        followingScript = RedisClient.loadScript("/META-INF/scripts/follow_following.lua");
        followerScript = RedisClient.loadScript("/META-INF/scripts/follow_follower.lua");
    }

    @Override
    public void processInsert(UserFollow entity) {
        //用户关注列表
        String following_list_key = Constants.LIST_USER_FOLLOWING + entity.getUserId();
        //用户粉丝列表
        String follower_list_key = Constants.LIST_USER_FOLLOWER + entity.getFollowing();

        addFollowing(following_list_key, entity.getUserId(), entity.getFollowing(), (double) entity.getMtime().getTime());
        addFollower(follower_list_key, entity.getFollowing(), entity.getUserId(), (double) entity.getMtime().getTime());
    }

    @Override
    public void processUpdate(UserFollow before, UserFollow after) {
        Byte oldState = before.getState();
        Byte state = after.getState();
        log.debug("before: {}, after: {}", before, after);
        if (Constants.ZERO.equals(oldState) && Constants.ONE.equals(state)) {
            //关注
            processInsert(after);
        } else {
            processDelete(after);
        }
    }

    @Override
    public void processDelete(UserFollow deletedEntity) {
        String following_list_key = Constants.LIST_USER_FOLLOWING + deletedEntity.getUserId(); //用户关注列表
        String follower_list_key = Constants.LIST_USER_FOLLOWER + deletedEntity.getFollowing(); //用户粉丝列表
        RedisClient.zRem(following_list_key, deletedEntity.getFollowing());
        RedisClient.zRem(follower_list_key, deletedEntity.getUserId());
    }

    private void addFollowing(String followingListKey, Integer userId, Integer following, double score) {
        boolean success = RedisClient.expire(followingListKey, RandomUtils.randomLong(10, 20), TimeUnit.HOURS);
        if (success) {
            Long res = RedisClient.executeScript(followingScript, List.of(followingListKey),
                    List.of(following, score, ZPageHandler.DEFAULT_MAX_SIZE));
            log.info("following-userId: {}, following: {}, res: {}", userId, following, res);
        } else {
            followService.loadUserFollowings(followingListKey, userId, -1, -1);
        }
    }

    private void addFollower(String followerListKey, Integer userId, Integer follower, double score) {
        boolean success = RedisClient.expire(followerListKey, RandomUtils.randomLong(10, 20), TimeUnit.HOURS);
        if (success) {
            Long res = RedisClient.executeScript(followerScript, List.of(followerListKey),
                    List.of(follower, score, ZPageHandler.DEFAULT_MAX_SIZE));
            log.info("following-userId: {}, following: {}, res: {}", userId, follower, res);
        } else {
            followService.loadUserFollowers(followerListKey, userId, -1, -1);
        }
    }
}