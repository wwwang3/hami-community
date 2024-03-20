package top.wang3.hami.core.service.user.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.wang3.hami.canal.CanalEntryHandler;
import top.wang3.hami.canal.annotation.CanalRabbitHandler;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.model.User;
import top.wang3.hami.common.util.RedisClient;


@Slf4j
@Component
@CanalRabbitHandler(value = "user", container = "canal-account-container-1")
public class UserCanalHandler implements CanalEntryHandler<User> {

    @Override
    public void processInsert(User entity) {
        processDelete(entity);
    }

    @Override
    public void processUpdate(User before, User after) {
        processDelete(after);
    }

    @Override
    public void processDelete(User deletedEntity) {
        String key = RedisConstants.USER_INFO + deletedEntity.getUserId();
        boolean success = RedisClient.deleteObject(key);
        log.info("delete user cache, key: {}, result: {}", key, success);
    }
}
