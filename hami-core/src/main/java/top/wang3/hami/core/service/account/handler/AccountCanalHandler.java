package top.wang3.hami.core.service.account.handler;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.wang3.hami.canal.CanalEntryHandler;
import top.wang3.hami.canal.annotation.CanalRabbitHandler;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.model.Account;
import top.wang3.hami.common.util.RedisClient;

@Slf4j
@Component
@CanalRabbitHandler(value = "account", container = "canal-account-container-1")
public class AccountCanalHandler implements CanalEntryHandler<Account> {

    @Override
    public void processInsert(Account entity) {
        processDelete(entity);
    }

    @Override
    public void processUpdate(Account before, Account after) {
        processDelete(after);
    }

    @Override
    public void processDelete(Account deletedEntity) {
        Integer id = deletedEntity.getId();
        String key = RedisConstants.ACCOUNT_INFO + id;
        boolean success = RedisClient.deleteObject(key);
        log.info("delete account cache, key: {}, result: {}", key, success);
    }
}
