package top.wang3.hami.core.service.interact.handler.count;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.wang3.hami.canal.annotation.CanalRabbitHandler;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.model.UserFollow;
import top.wang3.hami.core.cache.CacheService;
import top.wang3.hami.core.service.interact.FollowService;

@CanalRabbitHandler(value = "user_follow", container = "canal-interact-container-2")
@Component
@Slf4j
public class FollowerCountHandler extends AbstractInteractCountHandler<UserFollow> {

    private final FollowService followService;

    public FollowerCountHandler(CacheService cacheService, FollowService followService) {
        super(cacheService);
        this.followService = followService;
    }

    @Override
    public String buildKey(UserFollow entity) {
        return RedisConstants.USER_INTERACT_COUNT_HASH + entity.getFollowing();
    }

    @Override
    public String buildHkey(UserFollow entity) {
        return RedisConstants.FOLLOWER_INTERACT_HKEY;
    }

    @Override
    public boolean isInsert(UserFollow before, UserFollow after) {
        return Constants.ONE.equals(after.getState()) && Constants.ZERO.equals(before.getState());
    }

    @Override
    protected void loadCount(UserFollow entity) {
        followService.getUserFollowerCount(entity.getFollowing());
    }
}
