package top.wang3.hami.core.service.interact;

import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.request.PageParam;
import top.wang3.hami.common.model.ReadingRecord;
import top.wang3.hami.common.model.ReadingRecordDTO;

import java.util.List;

@SuppressWarnings("unused")
public interface ReadingRecordService extends IService<ReadingRecord> {

    boolean record(int userId, int articleId);

    boolean record(List<ReadingRecord> records);

    PageData<ReadingRecord> getReadingRecordByPage(PageParam param, Integer userId);

    PageData<ReadingRecordDTO> getReadingRecords(PageParam param);
}
