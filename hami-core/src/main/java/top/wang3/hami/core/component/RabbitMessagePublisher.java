package top.wang3.hami.core.component;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.message.RabbitMessage;

@Component
@RequiredArgsConstructor
@ConditionalOnBean(RabbitTemplate.class)
@SuppressWarnings("unused")
@Slf4j
public class RabbitMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    @Async
    public void publishMsg(RabbitMessage message) {
        if (message == null) return;
        rabbitTemplate.convertAndSend(message.getExchange(), message.getRoute(), message);
    }

    @Async
    public void publishMsg(RabbitMessage message, Object body) {
        if (message == null || body == null) return;
        rabbitTemplate.convertAndSend(message.getExchange(), message.getRoute(), body);
    }

    public void publishMsgSync(RabbitMessage message) {
        // 内部调用不走aop
        publishMsg(message);
    }

    public void publishMsgSync(RabbitMessage message, Object body) {
        publishMsg(message, body);
    }
}
