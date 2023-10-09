package top.wang3.hami.core.service.interact.repository;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.model.ReadingRecord;

import java.util.List;

public interface ReadingRecordRepository extends IService<ReadingRecord> {

    boolean record(int userId, int articleId);

    boolean record(List<ReadingRecord> records);

    boolean deleteRecord(int userId, Integer articleId);

    boolean deleteRecords(int userId, List<Integer> articleIds);

    boolean clearRecords(int userId);

    List<ReadingRecord> listReadingRecordByPage(Page<ReadingRecord> page, Integer userId);

    List<ReadingRecord> listReadingRecordByKeyword(Page<ReadingRecord> page, Integer userId, String keyword);

}
