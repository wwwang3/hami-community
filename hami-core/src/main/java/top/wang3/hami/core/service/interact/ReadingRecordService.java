package top.wang3.hami.core.service.interact;

import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.article.ReadingRecordDTO;
import top.wang3.hami.common.dto.request.SearchParam;

@SuppressWarnings("unused")
public interface ReadingRecordService {

    PageData<ReadingRecordDTO> listReadingRecords(SearchParam param);

}
