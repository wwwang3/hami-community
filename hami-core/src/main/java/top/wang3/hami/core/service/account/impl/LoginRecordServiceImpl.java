package top.wang3.hami.core.service.account.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.dto.IpInfo;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.model.LoginRecord;
import top.wang3.hami.core.mapper.LoginRecordMapper;
import top.wang3.hami.core.service.account.LoginRecordService;
import top.wang3.hami.security.context.LoginUserContext;
import top.wang3.hami.security.handler.AuthenticationEventHandler;
import top.wang3.hami.security.model.LoginUser;

import java.util.Date;
import java.util.List;

@Service
public class LoginRecordServiceImpl extends ServiceImpl<LoginRecordMapper, LoginRecord>
        implements LoginRecordService, AuthenticationEventHandler {

    @Override
    public PageData<LoginRecord> getRecordsByPage(PageParam param) {
        int userId = LoginUserContext.getLoginUserId();
        Page<LoginRecord> page = param.toPage();
        List<LoginRecord> records = ChainWrappers.queryChain(getBaseMapper())
                .select("id", "user_id", "ip_info", "login_time")
                .eq("user_id", userId)
                .orderByDesc("login_time")
                .list(page);
        page.setRecords(records);
        return PageData.build(page);
    }

    @Override
    public void handleSuccess(LoginUser user, IpInfo info, Date loginTime) {
        LoginRecord record = new LoginRecord();
        record.setUserId(user.getId());
        record.setLoginTime(loginTime);
        record.setIpInfo(info);
        super.save(record);
    }
}
