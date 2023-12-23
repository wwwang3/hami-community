package top.wang3.hami.core.service.user.comsumer;


import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.message.UserRabbitMessage;
import top.wang3.hami.common.util.RedisClient;

@RabbitListener(bindings = {
        @QueueBinding(
                value = @Queue("hami-user-queue-1"),
                exchange = @Exchange(value = RabbitConstants.HAMI_USER_EXCHANGE, type = "topic"),
                key = {"user.update", "user.create"}
        ),
}, concurrency = "2")
@Component
@RequiredArgsConstructor
public class UserMessageConsumer {

    @RabbitHandler
    public void handleMessage(UserRabbitMessage message) {
        String key = RedisConstants.USER_INFO + message.getUserId();
        RedisClient.deleteObject(key);
    }

}
