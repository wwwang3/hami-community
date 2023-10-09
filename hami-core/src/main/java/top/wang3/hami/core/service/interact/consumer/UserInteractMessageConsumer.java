package top.wang3.hami.core.service.interact.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.message.FollowRabbitMessage;

@Component
@RabbitListener(bindings = {
        @QueueBinding(
                value = @Queue("hami-user-interact-queue-3"),
                exchange = @Exchange(value = Constants.HAMI_TOPIC_EXCHANGE1, type = "topic"),
                key = {"do.follow", "do.like.*", "do.collect"}
        ),
})
@Slf4j
@RequiredArgsConstructor
public class UserInteractMessageConsumer {

    @RabbitHandler
    public void handleFollowMessage(FollowRabbitMessage message) {
        //关注
        log.debug("follow-message: {}", message);

    }
}
