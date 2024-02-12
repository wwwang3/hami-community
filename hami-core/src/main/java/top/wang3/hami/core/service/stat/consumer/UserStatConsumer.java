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
import top.wang3.hami.common.model.UserStat;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.service.stat.repository.UserStatRepository;
import top.wang3.hami.security.model.Result;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * 用户行为消费, 同步用户数据(总文章数, 总阅读量, 点赞数, 总评论数, 总收藏数, 总关注数, 总粉丝数)
 * 傻逼写法
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserStatConsumer {

    private final UserStatRepository userStatRepository;
    private final RabbitMessagePublisher rabbitMessagePublisher;

    @RabbitListener(
            id = "UserStatMsgContainer-1",
            bindings = {
                    @QueueBinding(
                            value = @Queue(RabbitConstants.USER_STAT_QUEUE_1),
                            exchange = @Exchange(value = RabbitConstants.HAMI_ARTICLE_EXCHANGE, type = ExchangeTypes.TOPIC),
                            key = {"article.view", "article.publish"}
                    ),
                    @QueueBinding(
                            value = @Queue(RabbitConstants.USER_STAT_QUEUE_2),
                            exchange = @Exchange(value = RabbitConstants.HAMI_INTERACT_EXCHANGE, type = ExchangeTypes.TOPIC),
                            key = {"*.like.1.*", "*.collect.*", "*.follow.*"}
                    ),
                    @QueueBinding(
                            value = @Queue(RabbitConstants.USER_STAT_QUEUE_3),
                            exchange = @Exchange(value = RabbitConstants.HAMI_COMMENT_EXCHANGE, type = ExchangeTypes.TOPIC),
                            key = {"comment.*"}
                    )
            },
            containerFactory = RabbitConstants.BATCH_LISTENER_FACTORY
    )
    public void handleStatMessage(List<RabbitMessage> messages) {
        try {
            // 不包含文章删除消息
            List<UserStat> userStats = messages.stream()
                    .collect(Collectors.groupingBy(this::getUserId))
                    .values()
                    .stream()
                    .map(objects -> {
                        UserStat userStat = new UserStat();
                        int userId = getUserId(objects.get(0));
                        userStat.setUserId(userId);
                        return objects.stream().reduce(userStat, this::handleObject, (v1, v2) -> v1);
                    })
                    .filter(stat -> stat.getUserId() != -1)
                    .toList();
            if (!userStats.isEmpty()){
                userStatRepository.batchUpdateUserStats(userStats);
            }
        } catch (Exception e) {
            log.error("error_class: {}, error_msg: {}", e.getClass(), e.getMessage());
            Map<String, Object> data = Map.of(
                    "error_class", e.getClass().getSimpleName(),
                    "error_msg", e.getMessage(),
                    "message", messages
            );
            String msgs = Result.writeValueAsString(data);
            AlarmEmailMessage message = new AlarmEmailMessage("用户数据更新失败", msgs);
            rabbitMessagePublisher.publishMsg(message);
        }
    }


    @RabbitListener(
            id = "UserStatMsgContainer-2",
            bindings = {
                    @QueueBinding(
                            value = @Queue(RabbitConstants.USER_STAT_QUEUE_4),
                            exchange = @Exchange(value = RabbitConstants.HAMI_INTERACT_EXCHANGE, type = "topic"),
                            key = "*.follow.*"
                    ),
            },
            containerFactory = RabbitConstants.BATCH_LISTENER_FACTORY
    )
    public void handleFollowMessage(List<FollowRabbitMessage> messages) {
        try {
            // 更新用户的总关注数
            List<UserStat> stats = messages.stream()
                    .collect(Collectors.groupingBy(FollowRabbitMessage::getUserId))
                    .values()
                    .stream()
                    .map(msgs -> {
                        UserStat stat = new UserStat();
                        stat.setUserId(msgs.get(0).getUserId());
                        return msgs.stream().reduce(stat, (a, b) -> {
                            a.setTotalFollowings(
                                    Optional.ofNullable(a.getTotalFollowings()).orElse(0) +
                                    delta(b.getState())
                            );
                            return a;
                        }, (v1, v2) -> v1);
                    }).toList();
            if (!stats.isEmpty()){
                userStatRepository.batchUpdateUserStats(stats);
            }
        } catch (Exception e) {
            log.error("error_class: {}, error_msg: {}", e.getClass(), e.getMessage());
            String msgs = Result.writeValueAsString(messages);
            AlarmEmailMessage message = new AlarmEmailMessage("用户关注数据更新失败", msgs);
            rabbitMessagePublisher.publishMsg(message);
        }
    }

    /**
     * shabi写法
     *
     * @param stat   stat
     * @param object object
     * @return stat
     */
    private UserStat handleObject(UserStat stat, Object object) {
        if (object instanceof ArticleRabbitMessage a) {
            // 文章消息
            ArticleRabbitMessage.Type type = a.getType();
            switch (type) {
                case PUBLISH -> stat.setTotalArticles(Optional.ofNullable(stat.getTotalArticles()).orElse(0) + 1);
                case DELETE -> stat.setTotalArticles(Optional.ofNullable(stat.getTotalArticles()).orElse(0) - 1);
                case VIEW -> stat.setTotalViews(Optional.ofNullable(stat.getTotalViews()).orElse(0) + 1);
            }
        } else if (object instanceof LikeRabbitMessage b) {
            // 点赞消息
            stat.setTotalLikes(Optional.ofNullable(stat.getTotalLikes()).orElse(0) + delta(b.getState()));
        } else if (object instanceof CommentRabbitMessage || object instanceof ReplyRabbitMessage) {
            // 评论消息
            stat.setTotalComments(Optional.ofNullable(stat.getTotalComments()).orElse(0) + 1);
        } else if (object instanceof CommentDeletedRabbitMessage c) {
            // 评论消息
            stat.setTotalComments(Optional.ofNullable(stat.getTotalComments()).orElse(0) - c.getDeletedCount());
        } else if (object instanceof CollectRabbitMessage d) {
            // 评论消息
            stat.setTotalCollects(Optional.ofNullable(stat.getTotalCollects()).orElse(0) + delta(d.getState()));
        } else if (object instanceof FollowRabbitMessage e) {
            // 关注消息
            stat.setTotalFollowers(Optional.ofNullable(stat.getTotalFollowers()).orElse(0) + delta(e.getState()));
        }
        return stat;
    }

    private int getUserId(Object o) {
        if (o instanceof ArticleRabbitMessage a) {
            return a.getAuthorId();
        } else if (o instanceof InteractRabbitMessage b) {
            return b.getToUserId();
        } else if (o instanceof CommentRabbitMessage c) {
            return c.getAuthorId();
        } else if (o instanceof ReplyRabbitMessage d) {
            return d.getAuthorId();
        } else if (o instanceof CommentDeletedRabbitMessage e) {
            return e.getAuthorId();
        }
        return -1;
    }

    private int delta(byte state) {
        return state == 1 ? 1 : -1;
    }

}
