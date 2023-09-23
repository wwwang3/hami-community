package top.wang3.hami.core.service.article.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.message.*;
import top.wang3.hami.core.service.article.ArticleStatService;

@Component
@RabbitListener(bindings = {
        @QueueBinding(
                value = @Queue("hami-user-interact-queue-1"),
                exchange = @Exchange(value = Constants.HAMI_TOPIC_EXCHANGE1, type = "topic"),
                key = {"*.like.1", "*.collect", "comment.*"}
        ),
})
@RequiredArgsConstructor
@Slf4j
//todo 消费失败先不管 _(≧∇≦」∠)_
public class UserInteractMessageConsumer {

    private final ArticleStatService articleStatService;

    @RabbitHandler
    public void handleLikeMessage(LikeRabbitMessage message) {
        //点赞消息
        //state表示取消还是点赞 对应路由do.like1 cancel.like.1
        log.debug("message: {}", message);
        if (message.isState()) {
            articleStatService.increaseLikes(message.getItemId(), 1);
        } else {
            articleStatService.decreaseLikes(message.getItemId(), 1);
        }
    }

    @RabbitHandler
    public void handleCollectMessage(CollectRabbitMessage message) {
        //收藏消息
        log.debug("message: {}", message);
        if (message.isState()) {
            articleStatService.increaseCollects(message.getArticleId(), 1);
        } else {
            articleStatService.decreaseCollects(message.getArticleId(), 1);
        }
    }

    @RabbitHandler
    public void handleCommentMessage(CommentRabbitMessage message) {
        //评论消息
        articleStatService.increaseComments(message.getArticleId(), 1);
    }

    @RabbitHandler
    public void handleReplyMessage(ReplyRabbitMessage message) {
        //回复
        articleStatService.increaseComments(message.getArticleId(), 1);
    }

    @RabbitHandler
    public void handleCommentDeletedMessage(CommentDeletedRabbitMessage message) {
        //评论删除
        //删除根评论的同时会删除子评论 所以要减去1+子评论数
        articleStatService.decreaseComments(message.getArticleId(), message.getDeletedCount());
    }
}
