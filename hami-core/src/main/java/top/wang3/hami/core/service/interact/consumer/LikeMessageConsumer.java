package top.wang3.hami.core.service.interact.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.message.interact.LikeRabbitMessage;
import top.wang3.hami.core.service.interact.repository.LikeRepository;


/**
 * 用户点赞行为消费
 * 将点赞操作写入数据库
 * 一个容器相当于一个消费者(concurrent设置为1), 多个绑定产生多个队列, 相当于消费多个队列的消息
 * 这里五个消费者, 消费不同主题的队列, 对于userId相同的消息, 路由到同一个队列, 保证对单个用户点赞写入和删除操作的顺序性
 */
@RabbitListener(
        id = "LikeMsgContainer",
        bindings = @QueueBinding(
                value = @Queue(RabbitConstants.LIKE_QUEUE_5),
                exchange = @Exchange(value = RabbitConstants.HAMI_INTERACT_EXCHANGE, type = ExchangeTypes.TOPIC),
                key = "*.like.*.*"
        )
)
@Component
@RequiredArgsConstructor
@Slf4j
public class LikeMessageConsumer {


    private final LikeRepository likeRepository;

    @RabbitHandler
    public void handleMessage(LikeRabbitMessage message) {
        if (message == null || message.getToUserId() == null) {
            return;
        }
        try {
            likeRepository.like(
                    message.getUserId(), message.getItemId(),
                    message.getLikeType(), message.getState()
            );
        } catch (Exception e) {
            // ignore it
            log.error("message: {}, error_class: {}, error_msg: {}", message, e.getClass(), e.getMessage());
        }
    }

}
