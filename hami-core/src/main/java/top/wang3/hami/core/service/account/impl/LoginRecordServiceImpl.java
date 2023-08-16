package top.wang3.hami.core.service.account.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.model.LoginRecord;
import top.wang3.hami.core.mapper.LoginRecordMapper;
import top.wang3.hami.core.service.account.LoginRecordService;

@Service
public class LoginRecordServiceImpl extends ServiceImpl<LoginRecordMapper, LoginRecord>
        implements LoginRecordService {
}
