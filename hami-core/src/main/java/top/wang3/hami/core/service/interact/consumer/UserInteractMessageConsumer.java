package top.wang3.hami.core.service.interact.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.message.*;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.component.InteractConsumer;
import top.wang3.hami.core.service.interact.repository.LikeRepository;

import java.util.List;


@RabbitListener(bindings = {
        @QueueBinding(
                value = @Queue(value = "hami-user-interact-queue-1"),
                exchange = @Exchange(value = RabbitConstants.HAMI_TOPIC_EXCHANGE1, type = "topic"),
                key = {"*.follow", "*.like.*", "*.collect"}
        )
}, concurrency = "2")
@Component
@Slf4j
@RequiredArgsConstructor
//todo 消费失败先不管 _(≧∇≦」∠)_
public class UserInteractMessageConsumer implements InteractConsumer {

    private final LikeRepository likeRepository;

    @Override
    public void handleLikeMessage(LikeRabbitMessage message) {
        String userLikeCountKey = RedisConstants.USER_LIKE_COUNT + message.getLikeType().getType() + ":" + message.getUserId();
        RedisClient.deleteObject(userLikeCountKey);
    }

    @Override
    public void handleCollectMessage(CollectRabbitMessage message) {
        String userCollectCountKey = RedisConstants.USER_COLLECT_COUNT + message.getUserId();
        RedisClient.deleteObject(userCollectCountKey);
    }

    @Override
    public void handleFollowMessage(FollowRabbitMessage message) {
        String followingCountKey = RedisConstants.USER_FOLLOWING_COUNT + message.getUserId();
        String followerCountKey = RedisConstants.USER_FOLLOWER_COUNT + message.getToUserId();
        RedisClient.deleteObject(List.of(followingCountKey, followerCountKey));
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

}
