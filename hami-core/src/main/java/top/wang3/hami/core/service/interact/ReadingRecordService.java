package top.wang3.hami.core.service.interact;

import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.SearchParam;
import top.wang3.hami.common.dto.article.ReadingRecordDTO;

import java.util.List;

@SuppressWarnings("unused")
public interface ReadingRecordService {

    PageData<ReadingRecordDTO> listReadingRecords(SearchParam param);

    boolean clearReadingRecords();

    boolean deleteRecord(Integer id);

    boolean deleteRecords(List<Integer> ids);

}
