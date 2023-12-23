package top.wang3.hami.core.service.stat.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.message.ArticleRabbitMessage;
import top.wang3.hami.common.message.interact.CollectRabbitMessage;
import top.wang3.hami.common.message.interact.InteractRabbitMessage;
import top.wang3.hami.common.message.interact.LikeRabbitMessage;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.core.service.stat.repository.ArticleStatRepository;
import top.wang3.hami.security.model.Result;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * 用户行为消息消费
 * 同步文章的数据
 * 对于文章点赞数的同步, 保证每次+1或者-1操作成功即可
 * 这里批量消费, 最少200ms写入一次, 最长10s写入一次
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleStatConsumer {

    private final ArticleStatRepository articleStatRepository;

    @RabbitListener(
            id = "StatMessageContainer-1",
            bindings = @QueueBinding(
                    value = @Queue(RabbitConstants.STAT_QUEUE_1),
                    exchange = @Exchange(value = RabbitConstants.HAMI_INTERACT_EXCHANGE, type = ExchangeTypes.TOPIC),
                    key = "*.like.1.*"
            ),
            containerFactory = "batchRabbitListenerContainerFactory"
    )
    public void handleLikeMessage(List<LikeRabbitMessage> messages) {
        try {
            List<ArticleStat> articleStats = messages.stream()
                    .filter(m -> m.getToUserId() != 0)
                    .collect(Collectors.groupingBy(InteractRabbitMessage::getItemId))
                    .values()
                    .stream()
                    .map(msgs -> msgs.stream().reduce(new ArticleStat(), (stat, msg) -> {
                                stat.setArticleId(msg.getItemId());
                                Integer likes = stat.getLikes();
                                int origin = likes == null ? 0 : likes;
                                stat.setLikes(origin + delta(msg.getState()));
                                return stat;
                            },
                            (v1, v2) -> v1
                    ))
                    .filter(s -> !Objects.equals(0, s.getLikes()))
                    .toList();
            articleStatRepository.batchUpdateLikes(articleStats);
        } catch (Exception e) {
            // todo 消费失败重试
            String msgs = Result.writeValueAsString(messages);
            log.error("messages: {}, error_class: {}, error_msg: {}", msgs, e.getClass(), e.getMessage());
        }
    }

    @RabbitListener(
            id = "StatMessageContainer-2",
            bindings = @QueueBinding(
                    value = @Queue(RabbitConstants.STAT_QUEUE_2),
                    exchange = @Exchange(value = RabbitConstants.HAMI_INTERACT_EXCHANGE, type = ExchangeTypes.TOPIC),
                    key = "*.collect.*"
            ),
            containerFactory = "batchRabbitListenerContainerFactory"
    )
    public void handleCollectMessage(List<CollectRabbitMessage> messages) {
        try {
            List<ArticleStat> articleStats = messages.stream()
                    .filter(m -> m.getToUserId() != 0)
                    .collect(Collectors.groupingBy(InteractRabbitMessage::getItemId))
                    .values()
                    .stream()
                    .map(msgs -> {
                        final ArticleStat articleStat = new ArticleStat();
                        articleStat.setArticleId(msgs.get(0).getItemId());
                        articleStat.setCollects(0);
                        return msgs.stream().reduce(articleStat, (stat, msg) -> {
                            stat.setCollects(stat.getCollects() + delta(msg.getState()));
                            return stat;
                        }, (v1, v2) -> v1);
                    })
                    .filter(s -> !Objects.equals(0, s.getCollects()))
                    .toList();
            articleStatRepository.batchUpdateCollects(articleStats);
        } catch (Exception e) {
            // todo 消费失败重试
            String msgs = Result.writeValueAsString(messages);
            log.error("messages: {}, error_class: {}, error_msg: {}", msgs, e.getClass(), e.getMessage());
        }
    }

    @RabbitListener(
            id = "StatMessageContainer-3",
            bindings = @QueueBinding(
                    value = @Queue(RabbitConstants.STAT_QUEUE_3),
                    exchange = @Exchange(value = RabbitConstants.HAMI_ARTICLE_EXCHANGE, type = ExchangeTypes.TOPIC),
                    key = {"article.view"}
            )
    )
    public void handleArticleDeletedMessage(ArticleRabbitMessage message) {
        // 文章删除消息
        articleStatRepository.deleteArticleStat(message.getArticleId());
    }

    @RabbitListener(
            id = "StatMessageContainer-4",
            bindings = @QueueBinding(
                    value = @Queue(RabbitConstants.STAT_QUEUE_3),
                    exchange = @Exchange(value = RabbitConstants.HAMI_ARTICLE_EXCHANGE, type = ExchangeTypes.TOPIC),
                    key = {"article.view"}
            ),
            containerFactory = "batchRabbitListenerContainerFactory"
    )
    public void handleArticleViewMessage(List<ArticleRabbitMessage> messages) {
        try {
            List<ArticleStat> articleStats = messages.stream()
                    .collect(Collectors.groupingBy(ArticleRabbitMessage::getArticleId))
                    .values()
                    .stream()
                    .map(msgs -> {
                        final ArticleStat articleStat = new ArticleStat();
                        articleStat.setArticleId(msgs.get(0).getArticleId());
                        articleStat.setViews(msgs.size());
                        return articleStat;
                    })
                    .filter(s -> !Objects.equals(0, s.getViews()))
                    .toList();
            articleStatRepository.batchUpdateViews(articleStats);
        } catch (Exception e) {
            // todo 消费失败重试
            String msgs = Result.writeValueAsString(messages);
            log.error("messages: {}, error_class: {}, error_msg: {}", msgs, e.getClass(), e.getMessage());
        }
    }

    private int delta(byte state) {
        return state == 1 ? 1 : -1;
    }

}
