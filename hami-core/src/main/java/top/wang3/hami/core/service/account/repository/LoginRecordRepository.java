package top.wang3.hami.core.service.account.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import top.wang3.hami.common.model.LoginRecord;

public interface LoginRecordRepository extends IService<LoginRecord> {

    @CanIgnoreReturnValue
    Page<LoginRecord> listLoginRecordByPage(Page<LoginRecord> page, Integer userId);
}
