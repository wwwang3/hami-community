package top.wang3.hami.core.service.interact.repository;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;
import top.wang3.hami.common.model.ReadingRecord;

import java.util.List;

public interface ReadingRecordRepository extends IService<ReadingRecord> {

    @Transactional(rollbackFor = Exception.class)
    boolean record(int userId, int articleId);

    boolean record(List<ReadingRecord> records);

    @Transactional(rollbackFor = Exception.class)
    boolean deleteRecord(int userId, Integer ids);

    @Transactional(rollbackFor = Exception.class)
    boolean deleteRecords(int userId, List<Integer> ids);

    @Transactional(rollbackFor = Exception.class)
    boolean clearRecords(int userId);

    List<ReadingRecord> listReadingRecordByPage(Page<ReadingRecord> page, Integer userId);

    List<ReadingRecord> listReadingRecordByKeyword(Page<ReadingRecord> page, Integer userId, String keyword);

}
