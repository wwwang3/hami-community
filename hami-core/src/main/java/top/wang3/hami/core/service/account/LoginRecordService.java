package top.wang3.hami.core.service.account;

import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.model.LoginRecord;

public interface LoginRecordService extends IService<LoginRecord> {

    PageData<LoginRecord> getRecordsByPage(PageParam param);
}
