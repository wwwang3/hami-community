package top.wang3.hami.core.service.account;

import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.dto.AccountInfo;
import top.wang3.hami.common.dto.request.RegisterParam;
import top.wang3.hami.common.dto.request.ResetPassParam;
import top.wang3.hami.common.model.Account;
import top.wang3.hami.core.exception.ServiceException;

public interface AccountService extends IService<Account> {

    Account getAccountByEmailOrUsername(String username);

    AccountInfo getAccountInfo();

    void register(RegisterParam param) throws ServiceException;

    void resetPassword(ResetPassParam param);

    void updatePassword(ResetPassParam param);

    boolean checkUsername(String username);


}
