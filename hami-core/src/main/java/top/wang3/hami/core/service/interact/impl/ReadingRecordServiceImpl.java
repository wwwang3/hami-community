package top.wang3.hami.core.service.interact.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.converter.ArticleConverter;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.article.ArticleDTO;
import top.wang3.hami.common.dto.article.ReadingRecordDTO;
import top.wang3.hami.common.dto.builder.ArticleOptionsBuilder;
import top.wang3.hami.common.dto.request.SearchParam;
import top.wang3.hami.common.message.ArticleRabbitMessage;
import top.wang3.hami.common.model.ReadingRecord;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.service.article.ArticleService;
import top.wang3.hami.core.service.interact.ReadingRecordService;
import top.wang3.hami.core.service.interact.repository.ReadingRecordRepository;
import top.wang3.hami.security.context.IpContext;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
@SuppressWarnings("unused")
@Slf4j
@RequiredArgsConstructor
public class ReadingRecordServiceImpl implements ReadingRecordService {

    private final ReadingRecordRepository readingRecordRepository;
    private final ArticleService articleService;
    private final RabbitMessagePublisher rabbitMessagePublisher;

    @Override
    public int record(Integer loginUserId, int articleId, int authorId) {
        //记录阅读数据
        String ip = IpContext.getIp();
        if (ip == null) return 0;
        String redisKey = Constants.VIEW_LIMIT + ip + ":" + articleId;
        boolean success = RedisClient.setNx(redisKey, "view-lock", 15, TimeUnit.SECONDS);
        if (!success) {
            log.debug("ip: {} access repeat", ip);
            return 0;
        }
        //发布消息
        ArticleRabbitMessage message = new ArticleRabbitMessage(ArticleRabbitMessage.Type.VIEW,
                articleId, authorId, loginUserId);
        rabbitMessagePublisher.publishMsg(message);
        return 1;
    }

    @Override
    public PageData<ReadingRecordDTO> listReadingRecords(SearchParam param) {
        String keyword = param.getKeyword();
        Page<ReadingRecord> page = param.toPage();
        int loginUserId = LoginUserContext.getLoginUserId();
        Collection<ReadingRecord> records;
        if (StringUtils.hasText(keyword)) {
           records = readingRecordRepository.listReadingRecordByKeyword(page, loginUserId, keyword);
        } else {
            records = readingRecordRepository.listReadingRecordByPage(page, loginUserId);
        }
        Collection<ReadingRecordDTO> dtos = ArticleConverter.INSTANCE.toReadingRecordDTO(records);
        buildReadingRecord(dtos);
        return PageData.<ReadingRecordDTO>builder()
                .total(page.getTotal())
                .pageNum(page.getCurrent())
                .data(dtos)
                .build();
    }

    private void buildReadingRecord(Collection<ReadingRecordDTO> dtos) {
        List<Integer> articleIds = ListMapperHandler.listTo(dtos, ReadingRecordDTO::getArticleId, false);
        List<ArticleDTO> articleDTOS = articleService.listArticleById(articleIds, new ArticleOptionsBuilder());
        ListMapperHandler.doAssemble(dtos, ReadingRecordDTO::getArticleId,
                articleDTOS, ArticleDTO::getId, ReadingRecordDTO::setContent);
    }
}
