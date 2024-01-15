package top.wang3.hami.core.service.account;

import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.model.LoginRecord;

public interface LoginRecordService {

    PageData<LoginRecord> listLoginRecordByPage(PageParam param);
}
