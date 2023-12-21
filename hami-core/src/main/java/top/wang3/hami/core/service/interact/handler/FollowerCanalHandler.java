package top.wang3.hami.core.service.interact.handler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import top.wang3.hami.canal.CanalEntryHandler;
import top.wang3.hami.canal.annotation.CanalRabbitHandler;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.constant.TimeoutConstants;
import top.wang3.hami.common.model.UserFollow;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.common.util.ZPageHandler;
import top.wang3.hami.core.service.interact.FollowService;

import java.util.List;

/**
 * 同步粉丝列表数据
 */
@Component
@CanalRabbitHandler(value = "user_follow", container = "canal-interact-container-1")
@RequiredArgsConstructor
@Slf4j
public class FollowerCanalHandler implements CanalEntryHandler<UserFollow> {


    private final FollowService followService;

    private RedisScript<Long> followerScript;

    @PostConstruct
    public void init() {
        followerScript = RedisClient.loadScript("/META-INF/scripts/follow_follower.lua");
    }

    @Override
    public void processInsert(UserFollow entity) {
        // 用户粉丝列表
        String follower_list_key = RedisConstants.USER_FOLLOWER_LIST + entity.getFollowing();
        // 被关注用户的粉丝列表元素+1
        addFollower(
                follower_list_key,
                entity.getFollowing(),
                entity.getUserId(),
                (double) entity.getMtime().getTime()
        );
    }

    @Override
    public void processUpdate(UserFollow before, UserFollow after) {
        Byte oldState = before.getState();
        Byte state = after.getState();
        if (isLogicDelete(oldState, state)) {
            // 关注
            processInsert(after);
        } else {
            processDelete(after);
        }
    }

    @Override
    public void processDelete(UserFollow deletedEntity) {
        // 用户粉丝列表
        String follower_list_key = RedisConstants.USER_FOLLOWER_LIST + deletedEntity.getFollowing();
        RedisClient.zRem(follower_list_key, deletedEntity.getUserId());
    }

    private void addFollower(String followerListKey, Integer userId, Integer follower, double score) {
        boolean success = RedisClient.pExpire(followerListKey, TimeoutConstants.FOLLOWER_LIST_EXPIRE);
        if (success) {
            Long res = RedisClient.executeScript(followerScript, List.of(followerListKey),
                    List.of(follower, score, ZPageHandler.DEFAULT_MAX_SIZE));
            log.info("userId: {}, follower: {}, res: {}", userId, follower, res);
        } else {
            followService.loadUserFollowers(followerListKey, userId, -1, -1);
        }
    }
}
