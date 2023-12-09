package top.wang3.hami.core.service.interact.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.message.interact.*;
import top.wang3.hami.common.util.RandomUtils;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.component.InteractConsumer;

import java.util.concurrent.TimeUnit;


/**
 * 用户行为消息消费者
 * 更新用户的点赞数, 收藏数, 关注数, 粉丝数
 */
@RabbitListener(bindings = {
        @QueueBinding(
                value = @Queue(value = RabbitConstants.USER_INTERACT_QUEUE_1),
                exchange = @Exchange(value = RabbitConstants.HAMI_LIKE_MESSAGE_EXCHANGE, type = ExchangeTypes.TOPIC),
                key = "*.like.*.*"
        ),
}, concurrency = "4")
@Component
@Slf4j
@RequiredArgsConstructor
//todo 消费失败先不管 _(≧∇≦」∠)_
public class UserInteractMessageConsumer implements InteractConsumer {

    @Override
    public void handleLikeMessage(LikeRabbitMessage message) {
        String userLikeCountKey = RedisConstants.USER_LIKE_COUNT + message.getLikeType().getType() + ":" + message.getUserId();
        if (message.getToUserId() != null) {
            execute(userLikeCountKey, message.getState());
        }
    }

    @Override
    public void handleCollectMessage(CollectRabbitMessage message) {
        String userCollectCountKey = RedisConstants.USER_COLLECT_COUNT + message.getUserId();
        if (message.getToUserId() != null) {
            execute(userCollectCountKey, message.getState());
        }
    }

    @Override
    public void handleFollowMessage(FollowRabbitMessage message) {
        String followingCountKey = RedisConstants.USER_FOLLOWING_COUNT + message.getUserId();
        String followerCountKey = RedisConstants.USER_FOLLOWER_COUNT + message.getToUserId();
        execute(followingCountKey, message.getState());
        execute(followerCountKey, message.getState());
    }

    @Override
    public void handleCommentMessage(CommentRabbitMessage message) {

    }

    @Override
    public void handleReplyMessage(ReplyRabbitMessage message) {

    }

    @Override
    public void handleCommentDeleteMessage(CommentDeletedRabbitMessage message) {

    }

    private void execute(String key, byte state) {
        long timeout = getTimeout();
        if (RedisClient.pExpire(key, timeout)) {
            RedisClient.incrBy(key, delta(state));
        } else {
            // 过期了啥都不做
            // ignore it
        }
    }

    private long getTimeout() {
        return TimeUnit.DAYS.toMillis(1) + RandomUtils.randomLong(10, 100000);
    }

    private int delta(byte state) {
        return state == 0 ? -1 : 1;
    }

}
