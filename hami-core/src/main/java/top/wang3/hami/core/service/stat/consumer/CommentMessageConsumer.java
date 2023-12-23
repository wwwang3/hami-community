package top.wang3.hami.core.service.stat.consumer;


import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.message.interact.CommentDeletedRabbitMessage;
import top.wang3.hami.common.message.interact.CommentRabbitMessage;
import top.wang3.hami.common.message.interact.ReplyRabbitMessage;
import top.wang3.hami.core.service.stat.ArticleStatService;

@RabbitListener(
        id = "StatMessageContainer-4",
        bindings = {
                @QueueBinding(
                        value = @Queue(value = RabbitConstants.STAT_QUEUE_4),
                        exchange = @Exchange(value = RabbitConstants.HAMI_INTERACT_EXCHANGE, type = "topic"),
                        key = {"comment.*"}
                )
        },
        concurrency = "2"
)
@Component
@RequiredArgsConstructor
public class CommentMessageConsumer {

    private final ArticleStatService articleStatService;

    @RabbitHandler
    public void handleCommentMessage(CommentRabbitMessage message) {
        articleStatService.increaseComments(message.getArticleId(), 1);
    }

    @RabbitHandler
    public void handleReplyMessage(ReplyRabbitMessage message) {
        articleStatService.increaseComments(message.getArticleId(), 1);
    }

    @RabbitHandler
    public void handleCommentDeleteMessage(CommentDeletedRabbitMessage message) {
        articleStatService.decreaseComments(message.getArticleId(), message.getDeletedCount());
    }

}
