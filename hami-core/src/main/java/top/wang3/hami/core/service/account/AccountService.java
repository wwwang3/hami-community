package top.wang3.hami.core.service.account;

import top.wang3.hami.common.dto.user.RegisterParam;
import top.wang3.hami.common.dto.user.ResetPassParam;
import top.wang3.hami.common.dto.user.UserProfileParam;
import top.wang3.hami.common.model.Account;
import top.wang3.hami.common.vo.user.AccountInfo;
import top.wang3.hami.core.exception.HamiServiceException;

public interface AccountService {

    Account getAccountByEmailOrUsername(String username);

    AccountInfo getAccountInfo();

    boolean register(RegisterParam param) throws HamiServiceException;

    boolean resetPassword(ResetPassParam param);

    boolean updatePassword(ResetPassParam param);

    boolean checkUsername(String username);

    boolean checkEmail(String email);

    boolean updateProfile(UserProfileParam param);
}
