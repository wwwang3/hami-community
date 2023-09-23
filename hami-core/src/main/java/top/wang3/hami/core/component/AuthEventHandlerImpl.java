package top.wang3.hami.core.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.dto.IpInfo;
import top.wang3.hami.common.model.LoginRecord;
import top.wang3.hami.core.service.account.LoginRecordService;
import top.wang3.hami.security.handler.AuthenticationEventHandler;
import top.wang3.hami.security.model.LoginUser;

import java.util.Date;

@Component
@Slf4j
public class AuthEventHandlerImpl implements AuthenticationEventHandler {


    private final LoginRecordService loginRecordService;

    public AuthEventHandlerImpl(LoginRecordService loginRecordService) {
        this.loginRecordService = loginRecordService;
    }

    @Override
    public void handleSuccess(LoginUser user, IpInfo info, Date loginTime) {
        LoginRecord record = new LoginRecord();
        record.setUserId(user.getId());
        record.setLoginTime(loginTime);
        record.setIpInfo(info);
        loginRecordService.save(record);
    }
}
