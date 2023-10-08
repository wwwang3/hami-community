package top.wang3.hami.core.service.interact;

import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.request.PageParam;
import top.wang3.hami.common.model.ReadingRecordDTO;

import java.util.List;

@SuppressWarnings("unused")
public interface ReadingRecordService {

    int record(int userId, int articleId, int authorId);

    PageData<ReadingRecordDTO> listReadingRecords(PageParam param);

    Long getUserReadingRecordCount(Integer userId);

    List<Integer> loadUserReadingRecordCache(String key, Integer userId, long current, long size);

}
