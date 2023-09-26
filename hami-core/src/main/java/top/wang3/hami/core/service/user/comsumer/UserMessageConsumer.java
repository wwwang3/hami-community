package top.wang3.hami.core.service.user.comsumer;


import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.message.ArticleRabbitMessage;
import top.wang3.hami.common.message.UserRabbitMessage;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.service.article.ReadingRecordService;

@RabbitListener(bindings = {
        @QueueBinding(
                value = @Queue("hami-user-queue-1"),
                exchange = @Exchange(value = Constants.HAMI_TOPIC_EXCHANGE2, type = "topic"),
                key = {"user.*", "article.view"}
        ),
}, concurrency = "4")
@Component
@RequiredArgsConstructor
public class UserMessageConsumer {

    private final ReadingRecordService readingRecordService;

    @RabbitHandler
    public void handleMessage(UserRabbitMessage message) {
        UserRabbitMessage.Type type = message.getType();
        if (type == UserRabbitMessage.Type.USER_UPDATE || type == UserRabbitMessage.Type.USER_DELETE) {
            String key = Constants.USER_INFO + message.getUserId();
            RedisClient.deleteObject(key);
        }
    }

    @RabbitHandler
    public void handleArticleViewMessage(ArticleRabbitMessage message) {
        Integer loginUserId = message.getLoginUserId();
        if (loginUserId == null) return;
        readingRecordService.record(loginUserId, message.getArticleId());
    }
}
