package top.wang3.hami.security.handler;

import top.wang3.hami.common.dto.IpInfo;
import top.wang3.hami.security.model.LoginUser;

import java.util.Date;

public interface AuthenticationEventHandler {

    void handleSuccess(LoginUser user, IpInfo info, Date loginTime);

}
