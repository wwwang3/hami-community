package top.wang3.hami.core.service.stat.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.message.interact.InteractRabbitMessage;
import top.wang3.hami.common.message.interact.LikeRabbitMessage;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.core.service.article.repository.ArticleStatRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 *  * 用户点赞行为消息消费
 *  * 同步文章点赞的数据
 */
@RabbitListeners(value = {
        @RabbitListener(
                id = "StatMessageContainer-1",
                bindings = @QueueBinding(
                        value = @Queue(RabbitConstants.STAT_QUEUE_1),
                        exchange = @Exchange(value = RabbitConstants.HAMI_LIKE_MESSAGE_EXCHANGE, type = ExchangeTypes.TOPIC),
                        key = "*.like.1.*"
                ),
                containerFactory = "batchRabbitListenerContainerFactory"
        ),
})
@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleLikesConsumer {

    private final ArticleStatRepository articleStatRepository;

    @RabbitHandler
    public void handleLikeMessage(List<LikeRabbitMessage> messages) {
        try {
            List<ArticleStat> articleStats = messages.stream()
                    .filter(m -> m.getToUserId() != 0)
                    .collect(Collectors.groupingBy(InteractRabbitMessage::getItemId))
                    .values()
                    .stream()
                    .map(msgs -> {
                        return msgs.stream().reduce(new ArticleStat(), (stat, msg) -> {
                            stat.setArticleId(msg.getItemId());
                            Integer likes = stat.getLikes();
                            int origin = likes == null ? 0 : likes;
                            stat.setLikes(origin + delta(msg.getState()));
                            return stat;
                        }, (v1, v2) -> {
                            return v1;
                        });
                    })
                    .filter(s -> !Objects.equals(0, s.getLikes()))
                    .toList();
            articleStatRepository.batchUpdateLikes(articleStats);
        } catch (Exception e) {
            // todo 消费失败
            log.error("error_class: {}, error_msg: {}", e.getClass(), e.getMessage());
        }
    }

    @RabbitHandler
    public void handleMessage(LikeRabbitMessage message) {
        if (message.getToUserId() == null) {
            return;
        }
        try {
            Integer itemId = message.getItemId();
            int delta = delta(message.getState());
            articleStatRepository.updateLikes(itemId, delta);
        } catch (Exception e) {
            // todo 消费失败处理
            // ignore it
        }

    }

    private int delta(byte state) {
        return state == 1 ? 1 : -1;
    }

}
