package top.wang3.hami.core.service.account.consumer;


import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.message.AccountRabbitMessage;
import top.wang3.hami.common.util.RedisClient;

@RabbitListener(bindings = {
        @QueueBinding(
                value = @Queue("hami-account-queue-1"),
                exchange = @Exchange(value = "hami-account-exchange", type = "topic"),
                key = {"account.*"}
        ),
})
@Component
public class AccountMessageConsumer {

    @RabbitHandler
    public void handleAccountMessage(AccountRabbitMessage message) {
        RedisClient.deleteObject(RedisConstants.ACCOUNT_INFO + message.getId());
    }

}
