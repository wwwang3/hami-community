package top.wang3.hami.core.service.user.comsumer;


import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.message.UserRabbitMessage;
import top.wang3.hami.common.util.RedisClient;

@RabbitListener(bindings = {
        @QueueBinding(
                value = @Queue("hami-user-queue-1"),
                exchange = @Exchange(value = Constants.HAMI_TOPIC_EXCHANGE2, type = "topic"),
                key = {"user.*"}
        ),
}, concurrency = "2")
@Component
@RequiredArgsConstructor
public class UserMessageConsumer {

    @RabbitHandler
    public void handleMessage(UserRabbitMessage message) {
        UserRabbitMessage.Type type = message.getType();
        if (type == UserRabbitMessage.Type.USER_UPDATE || type == UserRabbitMessage.Type.USER_DELETE) {
            String key = Constants.USER_INFO + message.getUserId();
            RedisClient.deleteObject(key);
        }
    }

}
