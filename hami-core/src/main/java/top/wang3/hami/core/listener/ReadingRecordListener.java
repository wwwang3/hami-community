package top.wang3.hami.core.listener;


import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.model.ReadingRecord;
import top.wang3.hami.core.service.article.ReadingRecordService;

import java.util.List;

@Component
@RabbitListener(queues = Constants.READING_RECORD_QUEUE,
        messageConverter = "rabbitMQJacksonConverter", containerFactory = "batchRabbitContainerFactory",
        concurrency = "4")
@RequiredArgsConstructor
public class ReadingRecordListener {

    private final ReadingRecordService readingRecordService;

    @RabbitHandler
    public void handleMessage(List<ReadingRecord> records) {
        //阅读记录
        for (ReadingRecord record : records) {
            //更新失败也不管了
            readingRecordService.record(record.getUserId(), record.getArticleId());
        }
    }
}
