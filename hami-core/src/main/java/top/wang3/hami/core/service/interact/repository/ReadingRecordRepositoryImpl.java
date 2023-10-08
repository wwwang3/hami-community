package top.wang3.hami.core.service.interact.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Repository;
import top.wang3.hami.common.model.ReadingRecord;
import top.wang3.hami.core.component.ZPageHandler;
import top.wang3.hami.core.mapper.ReadingRecordMapper;

import java.util.List;

@Repository
public class ReadingRecordRepositoryImpl extends ServiceImpl<ReadingRecordMapper, ReadingRecord>
        implements ReadingRecordRepository {

    @Override
    public boolean record(int userId, int articleId) {
        //阅读记录
        int rows = getBaseMapper().record(userId, articleId);
        return rows >= 1;
    }

    @Override
    public boolean record(List<ReadingRecord> records) {
        throw new UnsupportedOperationException("暂不支持");
    }

    @Override
    public boolean deleteRecord(int userId, Integer articleId) {
        //刪除阅读记录
        return ChainWrappers.updateChain(getEntityClass())
                .eq("user_id", userId)
                .eq("article_id", articleId)
                .remove();
    }

    @Override
    public boolean deleteRecords(int userId, List<Integer> articleIds) {
        return ChainWrappers.updateChain(getEntityClass())
                .eq("user_id", userId)
                .in("article_id", articleIds)
                .remove();
    }

    @Override
    public boolean clearRecords(int userId) {
        return ChainWrappers.updateChain(getEntityClass())
                .eq("user_id", userId)
                .remove();
    }

    @Override
    public Long getUserReadingRecordCount(Integer userId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .eq("user_id", userId)
                .count();
    }

    @Override
    public List<ReadingRecord> listReadingRecordByUserId(Integer userId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select( "user_id", "article_id", "reading_time")
                .eq("user_id", userId)
                .orderByDesc("reading_time")
                .last("limit " + ZPageHandler.DEFAULT_MAX_SIZE)
                .list();
    }

    @Override
    public List<ReadingRecord> listReadingRecordByPage(Page<ReadingRecord> page, Integer userId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("user_id", "reading_time", "article_id")
                .eq("user_id", userId)
                .orderByDesc("reading_time")
                .list(page);
    }
}
