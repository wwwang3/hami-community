package top.wang3.hami.core.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.model.IpInfo;
import top.wang3.hami.security.handler.AuthenticationEventHandler;
import top.wang3.hami.security.model.LoginUser;

import java.util.Date;

@Component
@Slf4j
public class AuthEventHandlerImpl implements AuthenticationEventHandler {

    @Override
    public void handleSuccess(LoginUser user, IpInfo info, Date loginTime) {
        log.debug("handle login success event, id: {}, ip: {}", user.getId(), info);
    }
}
