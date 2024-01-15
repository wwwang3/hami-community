package top.wang3.hami.core.service.account.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Repository;
import top.wang3.hami.common.model.LoginRecord;
import top.wang3.hami.core.mapper.LoginRecordMapper;

import java.util.List;

@Repository
public class LoginRecordRepositoryImpl extends ServiceImpl<LoginRecordMapper, LoginRecord>
        implements LoginRecordRepository {

    @Override
    public Page<LoginRecord> listLoginRecordByPage(Page<LoginRecord> page, Integer userId) {
        List<LoginRecord> records = ChainWrappers.queryChain(getBaseMapper())
                .select("id", "user_id", "ip_info", "login_time")
                .eq("user_id", userId)
                .orderByDesc("login_time")
                .list(page);
        page.setRecords(records);
        return page;
    }
}
