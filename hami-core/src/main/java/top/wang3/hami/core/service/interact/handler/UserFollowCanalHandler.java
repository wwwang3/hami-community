package top.wang3.hami.core.service.interact.handler;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.annotation.CanalListener;
import top.wang3.hami.common.canal.CanalEntryHandler;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.model.UserFollow;
import top.wang3.hami.common.util.RedisClient;

import java.util.List;

/**
 * 用户关注
 */
@Component
@CanalListener(value = "user_follow")
@Slf4j
public class UserFollowCanalHandler implements CanalEntryHandler<UserFollow> {

    private RedisScript<Long> followRedisScript;
    private RedisScript<Long> unFollowRedisScript;

    @PostConstruct
    public void loadFollowScript() {
        followRedisScript = RedisClient.loadScript("/META-INF/scripts/follow.lua");
        unFollowRedisScript = RedisClient.loadScript("/META-INF/scripts/unfollow.lua");
    }

    @Override
    public void processInsert(UserFollow entity) {
        //插入一条关注记录，
        //向Redis中用户的关注列表添加，和被关注用户的粉丝列表添加一条数据
        List<String> keys = getKeys(entity);

        //args
        Integer following_id = entity.getFollowing();
        Integer follower_id = entity.getUserId();

        Long score1 = entity.getMtime().getTime();
        Long score2 = System.currentTimeMillis();
        List<?> args = List.of(following_id, follower_id, score1, score2);
        RedisClient.executeScript(followRedisScript, keys, args);
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
        List<String> keys = getKeys(deletedEntity);

        Integer following_id = deletedEntity.getFollowing();
        Integer follower_id = deletedEntity.getUserId();

        RedisClient.executeScript(unFollowRedisScript, keys,
                List.of(following_id, follower_id));
    }
    
    private List<String> getKeys(UserFollow entity) {
        String following_list_key = Constants.LIST_USER_FOLLOWING + entity.getUserId(); //用户关注列表
        String follower_list_key = Constants.LIST_USER_FOLLOWER + entity.getFollowing(); //用户粉丝列表

        String user_stat_key = Constants.COUNT_TYPE_USER + entity.getUserId();
        String following_stat_key = Constants.COUNT_TYPE_USER + entity.getFollowing();
        return List.of(following_list_key, follower_list_key, user_stat_key, following_stat_key);
    }
}
