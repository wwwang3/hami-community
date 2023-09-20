package top.wang3.hami.core.component;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.model.NotifyMsg;

@Component
public class NotifyMsgPublisher {

    private final RabbitTemplate rabbitTemplate;

    public NotifyMsgPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishNotify(NotifyMsg o) {
        rabbitTemplate.convertAndSend(Constants.HAMI_DIRECT_EXCHANGE1, Constants.NOTIFY_ROUTING, o);
    }
}
