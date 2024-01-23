package top.wang3.hami.core.service.interact.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.message.interact.CollectRabbitMessage;
import top.wang3.hami.core.service.interact.repository.CollectRepository;


@RabbitListeners(value = {
        @RabbitListener(
                id = "CollectMessageContainer-1",
                bindings = @QueueBinding(
                        value = @Queue(RabbitConstants.COLLECT_QUEUE_1),
                        exchange = @Exchange(value = RabbitConstants.HAMI_INTERACT_EXCHANGE, type = ExchangeTypes.TOPIC),
                        key = "*.collect.*.1"
                )
        ),
        @RabbitListener(
                id = "CollectMessageContainer-2",
                bindings = @QueueBinding(
                        value = @Queue(RabbitConstants.COLLECT_QUEUE_2),
                        exchange = @Exchange(value = RabbitConstants.HAMI_INTERACT_EXCHANGE, type = ExchangeTypes.TOPIC),
                        key = "*.collect.*.2"
                )
        ),
        @RabbitListener(
                id = "CollectMessageContainer-3",
                bindings = @QueueBinding(
                        value = @Queue(RabbitConstants.COLLECT_QUEUE_3),
                        exchange = @Exchange(value = RabbitConstants.HAMI_INTERACT_EXCHANGE, type = ExchangeTypes.TOPIC),
                        key = "*.collect.*.3"
                )
        ),
        @RabbitListener(
                id = "CollectMessageContainer-4",
                bindings = @QueueBinding(
                        value = @Queue(RabbitConstants.COLLECT_QUEUE_4),
                        exchange = @Exchange(value = RabbitConstants.HAMI_INTERACT_EXCHANGE, type = ExchangeTypes.TOPIC),
                        key = "*.collect.*.4"
                )
        ),
        @RabbitListener(
                id = "CollectMessageContainer-5",
                bindings = @QueueBinding(
                        value = @Queue(RabbitConstants.COLLECT_QUEUE_5),
                        exchange = @Exchange(value = RabbitConstants.HAMI_INTERACT_EXCHANGE, type = ExchangeTypes.TOPIC),
                        key = "*.collect.*.5"
                )
        )
})
@Component
@RequiredArgsConstructor
@Slf4j
public class CollectMessageConsumer {

    private final CollectRepository collectRepository;

    @RabbitHandler
    public void handleCollectMessage(CollectRabbitMessage message) {
        if (message == null || message.getToUserId() == null) return;
        try {
            collectRepository.collectArticle(message.getUserId(), message.getItemId(), message.getState());
        } catch (Exception e) {
            log.error("message: {}, error_class: {}, error_msg: {}", message, e.getClass(), e.getMessage());
        }
    }
}
