package top.wang3.hami.core.service.interact.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.message.interact.FollowRabbitMessage;
import top.wang3.hami.core.service.interact.repository.FollowRepository;



/**
 * 用户点赞行为消费
 * 将点赞操作写入数据库
 * 一个容器相当于一个消费者(concurrent设置为1), 多个绑定产生多个队列, 相当于消费多个队列的消息
 * 这里五个消费者, 消费不同主题的队列, 对于userId相同的消息, 路由到同一个队列, 保证对单个用户点赞写入和删除操作的顺序性
 */
@RabbitListeners(value = {
        @RabbitListener(
                id = "FollowMessageContainer-1",
                bindings = @QueueBinding(
                        value = @Queue(RabbitConstants.FOLLOW_QUEUE_1),
                        exchange = @Exchange(value = RabbitConstants.HAMI_INTERACT_EXCHANGE, type = ExchangeTypes.TOPIC),
                        key = "*.follow.1"
                )
        ),
        @RabbitListener(
                id = "FollowMessageContainer-2",
                bindings = @QueueBinding(
                        value = @Queue(RabbitConstants.FOLLOW_QUEUE_2),
                        exchange = @Exchange(value = RabbitConstants.HAMI_INTERACT_EXCHANGE, type = ExchangeTypes.TOPIC),
                        key = "*.follow.2"
                )
        ),
        @RabbitListener(
                id = "FollowMessageContainer-3",
                bindings = @QueueBinding(
                        value = @Queue(RabbitConstants.FOLLOW_QUEUE_3),
                        exchange = @Exchange(value = RabbitConstants.HAMI_INTERACT_EXCHANGE, type = ExchangeTypes.TOPIC),
                        key = "*.follow.3"
                )
        ),
        @RabbitListener(
                id = "FollowMessageContainer-4",
                bindings = @QueueBinding(
                        value = @Queue(RabbitConstants.FOLLOW_QUEUE_4),
                        exchange = @Exchange(value = RabbitConstants.HAMI_INTERACT_EXCHANGE, type = ExchangeTypes.TOPIC),
                        key = "*.follow.4"
                )
        ),
        @RabbitListener(
                id = "FollowMessageContainer-5",
                bindings = @QueueBinding(
                        value = @Queue(RabbitConstants.FOLLOW_QUEUE_5),
                        exchange = @Exchange(value = RabbitConstants.HAMI_INTERACT_EXCHANGE, type = ExchangeTypes.TOPIC),
                        key = "*.follow.5"
                )
        )
})
@Component
@RequiredArgsConstructor
@Slf4j
public class FollowMessageConsumer {


    private final FollowRepository followRepository;

    @RabbitHandler
    public void handleMessage(FollowRabbitMessage message) {
        if (message == null || message.getToUserId() == null) return;
        try {
            boolean success = followRepository.followUser(message.getUserId(), message.getToUserId(), message.getState());
        } catch (Exception e) {
            // now ignore it
            log.error("message: {}, error_class: {}, error_msg: {}", message, e.getClass(), e.getMessage());
        }
    }
}
