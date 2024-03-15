package top.wang3.hami.core.mq;


import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.message.email.AlarmEmailMessage;
import top.wang3.hami.core.service.mail.MailMessageHandler;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RabbitListener(queues = {RabbitConstants.HAMI_DL_QUEUE})
@RequiredArgsConstructor
@Component
@Slf4j
public class DeadMessageConsumer {

    private final MailMessageHandler mailMessageHandler;

    @RabbitHandler
    public void handleMessage(Message message) {
        MessageProperties properties = message.getMessageProperties();
        try {
            Map<String, Serializable> data = Map.of(
                "message", new String(message.getBody(), StandardCharsets.UTF_8),
                "properties", properties
            );
            String msg = JSON.toJSONString(data);
            AlarmEmailMessage emailMessage = new AlarmEmailMessage("死信消息", msg);
            mailMessageHandler.handle(emailMessage);
        } catch (Exception e) {
            log.error("Failed to process dead letter message. error_class: {}, error_msg: {}, message: {}, properties: {}",
                    e.getClass().getName(),
                    e.getMessage(),
                    new String(message.getBody(), StandardCharsets.UTF_8),
                    properties
            );
        }
    }
}
