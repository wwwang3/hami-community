package top.wang3.hami.core.component;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.Constants;

@Component
public class NotifyMsgPublisher {

    private final RabbitTemplate rabbitTemplate;

    public NotifyMsgPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishNotify(Object o) {
        rabbitTemplate.convertAndSend(Constants.NOTIFY_EXCHANGE, Constants.NOTIFY_ROUTING, o);
    }
}
