package top.wang3.hami.core.service.interact.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.message.ArticleRabbitMessage;
import top.wang3.hami.common.message.FollowRabbitMessage;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.service.interact.repository.ReadingRecordRepository;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
//todo 消费失败先不管 _(≧∇≦」∠)_
public class UserInteractMessageConsumer {

    private final ReadingRecordRepository readingRecordRepository;

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue("hami-user-interact-queue-1"),
                    exchange = @Exchange(value = Constants.HAMI_TOPIC_EXCHANGE1, type = "topic"),
                    key = {"*.follow"}
            ),
    })
    public void handleFollowMessageForCount(FollowRabbitMessage message) {
        String followingCountKey = Constants.USER_FOLLOWING_COUNT + message.getUserId();
        String followerCountKey = Constants.USER_FOLLOWER_COUNT + message.getToUserId();
        RedisClient.deleteObject(List.of(followingCountKey, followerCountKey));
    }

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue("hami-user-interact-queue-2"),
                    exchange = @Exchange(value = Constants.HAMI_TOPIC_EXCHANGE2, type = "topic"),
                    key = {"article.view"}
            )
    })
    public void handleArticleReadingMessage(ArticleRabbitMessage message) {
        //写入用户阅读记录
        if (message.getLoginUserId() == null) return;
        boolean success = readingRecordRepository.record(message.getLoginUserId(), message.getArticleId());
        if (success) {
            log.debug("save user reading-record success: [{}-{}]", message.getLoginUserId(), message.getArticleId());
        }
    }

}
