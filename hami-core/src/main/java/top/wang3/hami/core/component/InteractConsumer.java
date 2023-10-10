package top.wang3.hami.core.component;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import top.wang3.hami.common.message.*;

public interface InteractConsumer {

    @RabbitHandler
    default void handleLikeMessage(LikeRabbitMessage message) {

    }

    @RabbitHandler
    default void handleCollectMessage(CollectRabbitMessage message) {

    }

    @RabbitHandler
    default void handleFollowMessage(FollowRabbitMessage message) {

    }

    @RabbitHandler
    default void handleCommentMessage(CommentRabbitMessage message) {

    }

    @RabbitHandler
    default void handleReplyMessage(ReplyRabbitMessage message) {

    }

    @RabbitHandler
    default void handleCommentDeleteMessage(CommentDeletedRabbitMessage message) {

    }
}
