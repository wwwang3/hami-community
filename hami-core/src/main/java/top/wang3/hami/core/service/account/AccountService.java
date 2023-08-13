package top.wang3.hami.core.service.account;

import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.model.Account;

public interface AccountService extends IService<Account> {

    Account getAccountByEmailOrUsername(String username);

//    Account register();
}
