package top.wang3.hami.core.service.account.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.dto.IpInfo;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.model.LoginRecord;
import top.wang3.hami.core.service.account.LoginRecordService;
import top.wang3.hami.core.service.account.repository.LoginRecordRepository;
import top.wang3.hami.security.context.LoginUserContext;
import top.wang3.hami.security.handler.AuthenticationEventHandler;
import top.wang3.hami.security.model.LoginUser;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class LoginRecordServiceImpl implements LoginRecordService, AuthenticationEventHandler {

    private final LoginRecordRepository loginRecordRepository;

    @Override
    public PageData<LoginRecord> listLoginRecordByPage(PageParam param) {
        int userId = LoginUserContext.getLoginUserId();
        Page<LoginRecord> page = param.toPage();
        loginRecordRepository.listLoginRecordByPage(page, userId);
        return PageData.build(page);
    }

    @Override
    public void handleSuccess(LoginUser user, IpInfo info, Date loginTime) {
        LoginRecord record = new LoginRecord();
        record.setUserId(user.getId());
        record.setLoginTime(loginTime);
        record.setIpInfo(info);
        loginRecordRepository.save(record);
    }
}
