package top.wang3.hami.core.component;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import top.wang3.hami.common.message.interact.*;

public interface InteractConsumer {

    @RabbitHandler
     void handleLikeMessage(LikeRabbitMessage message);

    @RabbitHandler
    void handleCollectMessage(CollectRabbitMessage message);

    @RabbitHandler
    void handleFollowMessage(FollowRabbitMessage message);

    @RabbitHandler
    void handleCommentMessage(CommentRabbitMessage message);

    @RabbitHandler
    void handleReplyMessage(ReplyRabbitMessage message);

    @RabbitHandler
    void handleCommentDeleteMessage(CommentDeletedRabbitMessage message);
}
