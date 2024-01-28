package top.wang3.hami.core.service.stat.handler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.wang3.hami.canal.CanalEntryHandler;
import top.wang3.hami.canal.annotation.CanalRabbitHandler;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.constant.TimeoutConstants;
import top.wang3.hami.common.converter.StatConverter;
import top.wang3.hami.common.model.UserStat;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.cache.CacheService;

@Component
@CanalRabbitHandler(value = "user_stat", container = "canal-stat-container-1")
@RequiredArgsConstructor
@Slf4j
public class UserStatHandler implements CanalEntryHandler<UserStat> {

    private final CacheService cacheService;

    @Override
    public void processInsert(UserStat entity) {
        setCache(entity);
    }

    @Override
    public void processUpdate(UserStat before, UserStat after) {
        // 更新 (删除缓存感觉更好)
        Byte oldState = before.getDeleted();
        Byte newState = after.getDeleted();
        if (isLogicDelete(oldState, newState)) {
            processDelete(after);
        } else {
            setCache(after);
        }
    }

    @Override
    public void processDelete(UserStat deletedEntity) {
        String redisKey = RedisConstants.STAT_TYPE_USER + deletedEntity.getUserId();
        RedisClient.deleteObject(redisKey);
    }

    private void setCache(UserStat stat) {
        String redisKey = RedisConstants.STAT_TYPE_USER + stat.getUserId();
        cacheService.refreshCache(
                redisKey,
                StatConverter.INSTANCE.toUserStatDTO(stat),
                TimeoutConstants.USER_STAT_EXPIRE
        );
    }
}
