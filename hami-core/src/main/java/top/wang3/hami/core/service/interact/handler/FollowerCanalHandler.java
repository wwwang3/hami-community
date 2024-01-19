package top.wang3.hami.core.service.interact.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.wang3.hami.canal.CanalEntryHandler;
import top.wang3.hami.canal.annotation.CanalRabbitHandler;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.constant.TimeoutConstants;
import top.wang3.hami.common.model.UserFollow;
import top.wang3.hami.common.util.InteractHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.common.util.ZPageHandler;
import top.wang3.hami.core.service.interact.FollowService;

/**
 * 同步粉丝列表数据
 */
@Component
@CanalRabbitHandler(value = "user_follow", container = "canal-interact-container-1")
@RequiredArgsConstructor
@Slf4j
public class FollowerCanalHandler implements CanalEntryHandler<UserFollow> {


    private final FollowService followService;

    @Override
    public void processInsert(UserFollow entity) {
        // 被关注用户的粉丝列表, 这里已经是MySQL插入插入成功后的数据
        Integer following = entity.getFollowing();
        String follower_list_key = RedisConstants.USER_FOLLOWER_LIST + following;
        // 被关注用户的粉丝列表元素+1
        InteractHandler
                .build("粉丝")
                .ofAction(follower_list_key, entity.getUserId(), entity.getMtime().getTime())
                .millis(TimeoutConstants.FOLLOWER_LIST_EXPIRE)
                .loader(() -> followService.loadUserFollowers(following))
                .postAct(() -> {
                    // 粉丝列表有长度限制
                    long size = RedisClient.zCard(follower_list_key);
                    if (size > ZPageHandler.DEFAULT_MAX_SIZE) {
                        RedisClient.zPopMin(follower_list_key);
                    }
                }).execute();
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
        // 被关注用户粉丝列表
        String follower_list_key = RedisConstants.USER_FOLLOWER_LIST + deletedEntity.getFollowing();
        RedisClient.zRem(follower_list_key, deletedEntity.getUserId());
    }
}
