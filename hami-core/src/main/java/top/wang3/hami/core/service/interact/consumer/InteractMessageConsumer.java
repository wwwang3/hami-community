package top.wang3.hami.core.service.interact.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.message.FollowRabbitMessage;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.service.interact.FollowService;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
//todo 消费失败先不管 _(≧∇≦」∠)_
public class InteractMessageConsumer {

    private final FollowService followService;

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue("hami-user-interact-queue-3"),
                    exchange = @Exchange(value = Constants.HAMI_TOPIC_EXCHANGE1, type = "topic"),
                    key = {"*.follow"}
            ),
    })
    public void handleFollowMessageForCount(FollowRabbitMessage message) {
        String followingKey = Constants.USER_FOLLOWING_COUNT + message.getUserId();
        String followerKey = Constants.USER_FOLLOWER_COUNT + message.getToUserId();
        RedisClient.deleteObject(List.of(followingKey, followerKey));
    }

}
