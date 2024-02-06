package top.wang3.hami.core.service.account.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.message.AccountRabbitMessage;
import top.wang3.hami.common.util.RedisClient;

@RabbitListener(
        id = "AccountMsgContainer-1",
        bindings = {
                @QueueBinding(
                        value = @Queue("hami-account-queue-1"),
                        exchange = @Exchange(value = RabbitConstants.HAMI_ACCOUNT_EXCHANGE, type = "topic"),
                        key = {"account.*"}
                )
        })
@Component
@Slf4j
@RequiredArgsConstructor
public class AccountMessageConsumer {

    @RabbitHandler
    public void handleAccountMessage(AccountRabbitMessage message) {
        RedisClient.deleteObject(RedisConstants.ACCOUNT_INFO + message.getId());
        if (log.isDebugEnabled()) {
            log.debug("delete account cache success, id: {}", message.getId());
        }
    }

}
