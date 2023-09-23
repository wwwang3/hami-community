package top.wang3.hami.core.component;


import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.message.RabbitMessage;

@Component
@RequiredArgsConstructor
public class RabbitMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishMsg(RabbitMessage message) {
        if (message == null) return;
        rabbitTemplate.convertAndSend(message.getExchange(), message.getRoute(), message);
    }

}
