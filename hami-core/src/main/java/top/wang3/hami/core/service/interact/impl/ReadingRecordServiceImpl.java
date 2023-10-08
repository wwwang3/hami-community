package top.wang3.hami.core.service.interact.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.zset.Tuple;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.article.ArticleDTO;
import top.wang3.hami.common.dto.builder.ArticleOptionsBuilder;
import top.wang3.hami.common.dto.request.PageParam;
import top.wang3.hami.common.message.ArticleRabbitMessage;
import top.wang3.hami.common.model.ReadingRecord;
import top.wang3.hami.common.model.ReadingRecordDTO;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RandomUtils;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.component.ZPageHandler;
import top.wang3.hami.core.service.article.ArticleService;
import top.wang3.hami.core.service.interact.ReadingRecordService;
import top.wang3.hami.core.service.interact.repository.ReadingRecordRepository;
import top.wang3.hami.security.context.IpContext;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.Date;
import java.util.List;
import java.util.Objects;
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
    public int record(int userId, int articleId, int authorId) {
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
        Integer loginUserId = LoginUserContext.getLoginUserIdDefaultNull();
        ArticleRabbitMessage message = new ArticleRabbitMessage(ArticleRabbitMessage.Type.VIEW,
                articleId, authorId, loginUserId);
        rabbitMessagePublisher.publishMsg(message);
        return 1;
    }

    @Override
    public PageData<ReadingRecordDTO> listReadingRecords(PageParam param) {
        int loginUserId = LoginUserContext.getLoginUserId();
        String key = Constants.READING_RECORD_LIST + loginUserId;
        Page<ReadingRecordDTO> page = param.toPage();


        ZPageHandler.<>of(key, page, this)
                .countSupplier(() -> this.getUserReadingRecordCount(loginUserId))
                .source((current, size) -> {
                    Page<ReadingRecord> itemPage = new Page<>(current, size, false);
                    return readingRecordRepository.listReadingRecordByPage(itemPage, loginUserId);
                })
                .query()

        List<ReadingRecordDTO> dtos = ListMapperHandler.listTo(tuples, item -> {
            Integer articleId = item.getValue();
            Date readingTime = new Date(Objects.requireNonNull(item.getScore()).longValue());
            return new ReadingRecordDTO(loginUserId, articleId, readingTime, null);
        }, false);
        return PageData.build(page)
    }

    @Override
    public Long getUserReadingRecordCount(Integer userId) {
        String redisKey = Constants.READING_RECORD_COUNT + userId;
        Long count = RedisClient.getCacheObject(redisKey);
        if (count == null) {
            synchronized (this) {
                count = RedisClient.getCacheObject(redisKey);
                if (count == null) {
                    count = readingRecordRepository.getUserReadingRecordCount(userId);
                    RedisClient.setCacheObject(redisKey, count, RandomUtils.randomLong(20, 30), TimeUnit.DAYS);
                }
            }
        }
        return count;
    }

    @Override
    public List<Integer> loadUserReadingRecordCache(String key, Integer userId, long current, long size) {
        List<ReadingRecord> records = readingRecordRepository.listReadingRecordByUserId(userId);
        List<Tuple> tuples = ListMapperHandler.listToTuple(records, ReadingRecord::getArticleId,
                record -> record.getReadingTime().getTime());
        RedisClient.zSetAll(key, tuples, RandomUtils.randomLong(20, 30), TimeUnit.DAYS);
        return ListMapperHandler.subList(records, ReadingRecord::getArticleId, current, size);
    }

    private void buildReadingRecord(List<ReadingRecordDTO> dtos) {
        List<Integer> articleIds = ListMapperHandler.listTo(dtos, ReadingRecordDTO::getArticleId, false);
        List<ArticleDTO> articleDTOS = articleService.listArticleById(articleIds, new ArticleOptionsBuilder());
        ListMapperHandler.doAssemble(dtos, ReadingRecordDTO::getArticleId,
                articleDTOS, ArticleDTO::getId, ReadingRecordDTO::setContent);
    }
}
