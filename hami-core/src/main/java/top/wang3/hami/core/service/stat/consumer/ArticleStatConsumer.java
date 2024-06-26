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
import top.wang3.hami.common.message.RabbitMessage;
import top.wang3.hami.common.message.email.AlarmEmailMessage;
import top.wang3.hami.common.message.interact.*;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.service.stat.repository.ArticleStatRepository;
import top.wang3.hami.security.model.Result;

import java.util.List;
import java.util.Map;
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
    private final RabbitMessagePublisher rabbitMessagePublisher;

    @RabbitListener(
        id = "ArticleStatMsgContainer-1",
        bindings = @QueueBinding(
            value = @Queue(RabbitConstants.STAT_QUEUE_1),
            exchange = @Exchange(value = RabbitConstants.HAMI_INTERACT_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = "*.like.1.*"
        ),
        containerFactory = RabbitConstants.BATCH_LISTENER_FACTORY
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
                .filter(s -> check(s.getArticleId(), -1) && check(s.getLikes(), 0))
                .toList();
            if (articleStats.isEmpty()) {
                return;
            }
            articleStatRepository.batchUpdateLikes(articleStats);
        } catch (Exception e) {
            log.error("error_class: {}, error_msg: {}", e.getClass(), e.getMessage());
            Map<String, Object> map = Map.of(
                "error_class", e.getClass().getName(),
                "error_msg", e.getMessage(),
                "rabbit_msg", messages
            );
            String msgs = Result.writeValueAsString(map);
            AlarmEmailMessage message = new AlarmEmailMessage("更新文章点赞失败", msgs);
            rabbitMessagePublisher.publishMsgSync(message);
        }
    }

    @RabbitListener(
        id = "ArticleStatMessageContainer-2",
        bindings = @QueueBinding(
            value = @Queue(RabbitConstants.STAT_QUEUE_2),
            exchange = @Exchange(value = RabbitConstants.HAMI_COMMENT_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = "comment.*"
        ),
        containerFactory = RabbitConstants.BATCH_LISTENER_FACTORY
    )
    public void handleCommentMessage(List<RabbitMessage> messages) {
        try {
            List<ArticleStat> articleStats = messages.stream()
                .collect(Collectors.groupingBy(this::getCommentArticleId))
                .values()
                .stream()
                .map(msgs -> {
                    final ArticleStat articleStat = new ArticleStat();
                    articleStat.setArticleId(getCommentArticleId(msgs.get(0)));
                    articleStat.setComments(0);
                    return msgs.stream().reduce(articleStat, this::handleCommentMessage, (v1, v2) -> v1);
                })
                .filter(s -> check(s.getArticleId(), -1) && check(s.getComments(), 0))
                .toList();
            if (articleStats.isEmpty()) {
                return;
            }
            articleStatRepository.batchUpdateComments(articleStats);
        } catch (Exception e) {
            log.error("error_class: {}, error_msg: {}", e.getClass(), e.getMessage());
            Map<String, Object> map = Map.of(
                "error_class", e.getClass().getName(),
                "error_msg", e.getMessage(),
                "rabbit_msg", messages
            );
            String msgs = Result.writeValueAsString(map);
            AlarmEmailMessage message = new AlarmEmailMessage("更新文章评论数据失败", msgs);
            rabbitMessagePublisher.publishMsgSync(message);
        }
    }

    @RabbitListener(
        id = "ArticleStatMessageContainer-3",
        bindings = @QueueBinding(
            value = @Queue(RabbitConstants.STAT_QUEUE_3),
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
                .filter(s -> check(s.getArticleId(), -1) && check(s.getCollects(), 0))
                .toList();
            if (articleStats.isEmpty()) {
                return;
            }
            articleStatRepository.batchUpdateCollects(articleStats);
        } catch (Exception e) {
            log.error("error_class: {}, error_msg: {}", e.getClass(), e.getMessage());
            Map<String, Object> map = Map.of(
                "error_class", e.getClass().getName(),
                "error_msg", e.getMessage(),
                "rabbit_msg", messages
            );
            String msgs = Result.writeValueAsString(map);
            AlarmEmailMessage message = new AlarmEmailMessage("更新文章收藏数据失败", msgs);
            rabbitMessagePublisher.publishMsgSync(message);
        }
    }

    @RabbitListener(
        id = "ArticleStatMessageContainer-4",
        bindings = @QueueBinding(
            value = @Queue(RabbitConstants.STAT_QUEUE_4),
            exchange = @Exchange(value = RabbitConstants.HAMI_ARTICLE_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = {"article.view"}
        ),
        containerFactory = RabbitConstants.BATCH_LISTENER_FACTORY
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
                .filter(s -> check(s.getArticleId(), -1) && check(s.getViews(), 0))
                .toList();
            if (articleStats.isEmpty()) {
                return;
            }
            articleStatRepository.batchUpdateViews(articleStats);
        } catch (Exception e) {
            log.error("error_class: {}, error_msg: {}", e.getClass(), e.getMessage());
            Map<String, Object> map = Map.of(
                "error_class", e.getClass().getName(),
                "error_msg", e.getMessage(),
                "rabbit_msg", messages
            );
            String msgs = Result.writeValueAsString(map);
            AlarmEmailMessage message = new AlarmEmailMessage("更新文章阅读量失败", msgs);
            rabbitMessagePublisher.publishMsgSync(message);
        }
    }

    private int delta(byte state) {
        return state == 1 ? 1 : -1;
    }

    private ArticleStat handleCommentMessage(ArticleStat articleStat, RabbitMessage message) {
        if (message instanceof CommentRabbitMessage || message instanceof ReplyRabbitMessage) {
            articleStat.setComments(articleStat.getComments() + 1);
        } else if (message instanceof CommentDeletedRabbitMessage c) {
            articleStat.setComments(articleStat.getComments() - c.getDeletedCount());
        }
        return articleStat;
    }

    private Integer getCommentArticleId(RabbitMessage message) {
        if (message instanceof CommentRabbitMessage c) {
            return c.getArticleId();
        } else if (message instanceof ReplyRabbitMessage r) {
            return r.getArticleId();
        } else if (message instanceof CommentDeletedRabbitMessage c) {
            return c.getArticleId();
        } else {
            return -1;
        }
    }

    private boolean check(Integer value, int unExcepted) {
        return value != null && value != unExcepted;
    }

}
