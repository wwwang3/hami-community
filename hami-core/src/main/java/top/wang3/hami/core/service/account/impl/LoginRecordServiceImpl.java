package top.wang3.hami.core.service.account.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.model.LoginRecord;
import top.wang3.hami.core.service.account.LoginRecordService;
import top.wang3.hami.core.service.account.repository.LoginRecordRepository;
import top.wang3.hami.security.context.LoginUserContext;

@Service
@RequiredArgsConstructor
public class LoginRecordServiceImpl implements LoginRecordService {

    private final LoginRecordRepository loginRecordRepository;

    @Override
    public PageData<LoginRecord> listLoginRecordByPage(PageParam param) {
        int userId = LoginUserContext.getLoginUserId();
        Page<LoginRecord> page = param.toPage();
        loginRecordRepository.listLoginRecordByPage(page, userId);
        return PageData.build(page);
    }

}
