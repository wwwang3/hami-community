package top.wang3.hami.core.service.article.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import top.wang3.hami.common.converter.ArticleConverter;
import top.wang3.hami.common.dto.ArticleDTO;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.request.PageParam;
import top.wang3.hami.common.model.ReadingRecord;
import top.wang3.hami.common.model.ReadingRecordDTO;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.core.mapper.ReadingRecordMapper;
import top.wang3.hami.core.service.article.ArticleService;
import top.wang3.hami.core.service.article.ReadingRecordService;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.List;


@Service
@SuppressWarnings("unused")
@Slf4j
@RequiredArgsConstructor
public class ReadingRecordServiceImpl extends ServiceImpl<ReadingRecordMapper, ReadingRecord>
        implements ReadingRecordService {
    @Resource
    TransactionTemplate transactionTemplate;

    private final ArticleService articleService;

    @Transactional
    public boolean record(List<ReadingRecord> records) {
        //
        throw new UnsupportedOperationException("暂不支持");
//        return super.saveBatch(records);
    }

    @Override
    public PageData<ReadingRecord> getReadingRecordByPage(PageParam param, Integer userId) {
        Page<ReadingRecord> page = param.toPage();
        page = ChainWrappers.queryChain(getBaseMapper())
                .select("user_id", "article_id", "reading_time")
                .eq("user_id", userId)
                .orderByDesc("reading_time")
                .page(page);
        return PageData.build(page);
    }

    @Override
    public PageData<ReadingRecordDTO> getReadingRecords(PageParam param) {
        int loginUserId = LoginUserContext.getLoginUserId();
        PageData<ReadingRecord> page = this.getReadingRecordByPage(param, loginUserId);
        List<ReadingRecord> records = page.getData();
        if (records == null || records.isEmpty()) {
            return null;
        }
        List<Integer> articleIds = ListMapperHandler.listTo(records, ReadingRecord::getArticleId);
        ArticleService.OptionsBuilder builder = new ArticleService.OptionsBuilder()
                .noTags()
                .noInteract();
        List<ArticleDTO> articleDTOS = articleService.getArticleByIds(articleIds, builder);
        List<ReadingRecordDTO> data = ArticleConverter.INSTANCE.toReadingRecordDTO(records);
        ListMapperHandler.doAssemble(data, ReadingRecordDTO::getArticleId, articleDTOS,
                ArticleDTO::getId, ReadingRecordDTO::setContent);
        return PageData.<ReadingRecordDTO>builder()
                .total(page.getTotal())
                .pageNum(page.getPageNum())
                .data(data)
                .build();
    }

    @Override
    public boolean record(int userId, int articleId) {
        ReadingRecord record = getReadingRecord(userId, articleId);
        Boolean success = transactionTemplate.execute(status -> {
            ReadingRecord readingRecord = new ReadingRecord(userId, articleId);
            boolean saved = false;
            if (record == null) {
                //没有记录尝试新增
                 saved = trySave(readingRecord);
            }
            //新增成功
            if (saved) return true;
            //插入记录失败
            return tryUpdate(readingRecord);
        });
        return Boolean.TRUE.equals(success);
    }

    private boolean trySave(ReadingRecord readingRecord) {
        try {
            return super.save(readingRecord);
        } catch (Exception e) {
            log.warn("save failed: error_class: {} error_msg: {}", e.getClass().getCanonicalName(), e.getMessage());
            return false;
        }
    }

    private boolean tryUpdate(ReadingRecord record) {
        return ChainWrappers.updateChain(getBaseMapper())
                .eq("user_id", record.getUserId())
                .eq("article_id", record.getArticleId())
                .update();
    }

    public ReadingRecord getReadingRecord(int userId, int articleId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .eq("user_id", userId)
                .eq("article_id", articleId)
                .one();
    }
}
