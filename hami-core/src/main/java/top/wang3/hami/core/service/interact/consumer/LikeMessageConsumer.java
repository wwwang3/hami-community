package top.wang3.hami.core.service.interact.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.dto.interact.LikeType;
import top.wang3.hami.common.message.interact.LikeRabbitMessage;
import top.wang3.hami.core.exception.HamiServiceException;
import top.wang3.hami.core.service.interact.repository.LikeRepository;


/**
 * 用户点赞行为消费
 * 将点赞操作写入数据库
 */
@RabbitListeners(value = {
        @RabbitListener(
                id = "LikeMessageContainer-1",
                bindings = @QueueBinding(
                        value = @Queue(RabbitConstants.LIKE_QUEUE_1),
                        exchange = @Exchange(value = RabbitConstants.HAMI_LIKE_MESSAGE_EXCHANGE, type = ExchangeTypes.TOPIC),
                        key = "*.like.*.1"
                )
        ),
        @RabbitListener(
                id = "LikeMessageContainer-2",
                bindings = @QueueBinding(
                        value = @Queue(RabbitConstants.LIKE_QUEUE_2),
                        exchange = @Exchange(value = RabbitConstants.HAMI_LIKE_MESSAGE_EXCHANGE, type = ExchangeTypes.TOPIC),
                        key = "*.like.*.2"
                )
        ),
        @RabbitListener(
                id = "LikeMessageContainer-3",
                bindings = @QueueBinding(
                        value = @Queue(RabbitConstants.LIKE_QUEUE_3),
                        exchange = @Exchange(value = RabbitConstants.HAMI_LIKE_MESSAGE_EXCHANGE, type = ExchangeTypes.TOPIC),
                        key = "*.like.*.3"
                )
        ),
        @RabbitListener(
                id = "LikeMessageContainer-4",
                bindings = @QueueBinding(
                        value = @Queue(RabbitConstants.LIKE_QUEUE_4),
                        exchange = @Exchange(value = RabbitConstants.HAMI_LIKE_MESSAGE_EXCHANGE, type = ExchangeTypes.TOPIC),
                        key = "*.like.*.4"
                )
        ),
        @RabbitListener(
                id = "LikeMessageContainer-5",
                bindings = @QueueBinding(
                        value = @Queue(RabbitConstants.LIKE_QUEUE_5),
                        exchange = @Exchange(value = RabbitConstants.HAMI_LIKE_MESSAGE_EXCHANGE, type = ExchangeTypes.TOPIC),
                        key = "*.like.*.5"
                )
        )
})
@Component
@RequiredArgsConstructor
@Slf4j
public class LikeMessageConsumer {


    private final LikeRepository likeRepository;

    @RabbitHandler
    public void handleMessage(LikeRabbitMessage message) {
        try {
            if (message.getToUserId() == null) {
                return;
            }
            int userId = message.getUserId();
            Integer itemId = message.getItemId();
            LikeType likeType = message.getLikeType();
            if (message.getState() == Constants.ONE) {
                likeRepository.doLike(userId, itemId, likeType);
            } else {
                likeRepository.cancelLike(userId, itemId, likeType);
            }
        } catch (HamiServiceException e) {
            // ignore it
        } catch (Exception e) {
            // todo 消费失败处理
            // ignore it
            log.error("error_class: {}, error_msg: {}", e.getClass(), e.getMessage());
        }
    }

}
