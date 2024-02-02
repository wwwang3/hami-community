package top.wang3.hami.core.init;


import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.constant.TimeoutConstants;
import top.wang3.hami.common.model.Account;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.mapper.AccountMapper;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class AccountCacheInitializer implements HamiInitializer {

    private final AccountMapper accountMapper;

    public AccountCacheInitializer(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    @Override
    public InitializerEnums getName() {
        return InitializerEnums.ACCOUNT_CACHE;
    }

    @Override
    public boolean alwaysExecute() {
        return true;
    }

    @Override
    public boolean async() {
        return true;
    }

    @Override
    public void run() {
        ListMapperHandler.scanDesc(
                Integer.MAX_VALUE,
                100,
                1000,
                accountMapper::scanAccountDesc,
                accounts -> {
                    Map<String, Account> map = ListMapperHandler.listToMap(accounts,
                            item -> RedisConstants.ACCOUNT_INFO + item.getId());
                    RedisClient.cacheMultiObject(map, TimeoutConstants.ACCOUNT_INFO_EXPIRE, TimeUnit.MILLISECONDS);
                },
                Account::getId
        );
    }
}
