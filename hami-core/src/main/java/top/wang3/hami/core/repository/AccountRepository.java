package top.wang3.hami.core.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.model.Account;

public interface AccountRepository extends IService<Account> {

    Account getAccountByEmailOrUsername(String account);

    Account getAccountInfo(Integer userId);

    boolean updatePassword(String email, String old, String newPassword);

    boolean checkUsername(String username);

    boolean checkEmail(String email);
}
