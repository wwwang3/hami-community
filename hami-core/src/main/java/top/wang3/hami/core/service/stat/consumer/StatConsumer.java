package top.wang3.hami.core.service.stat.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.message.*;
import top.wang3.hami.core.component.InteractConsumer;
import top.wang3.hami.core.service.article.ArticleStatService;
import top.wang3.hami.core.service.article.repository.ArticleRepository;

@Component
@RabbitListener(bindings = {
        @QueueBinding(
                value = @Queue(value = "hami-stat-queue-1"),
                exchange = @Exchange(value = Constants.HAMI_TOPIC_EXCHANGE1, type = "topic"),
                key = {"comment.*"}
        ),
        @QueueBinding(
                value = @Queue(value = "hami-stat-queue-2"),
                exchange = @Exchange(value = Constants.HAMI_TOPIC_EXCHANGE2, type = "topic"),
                key = {"article.delete"}
        )
})
@RequiredArgsConstructor
@Slf4j
public class StatConsumer implements InteractConsumer {

    private final ArticleRepository articleRepository;
    private final ArticleStatService articleStatService;

    @RabbitHandler
    public void handleArticleMessage(ArticleRabbitMessage message) {
        articleRepository.deleteArticle(message.getArticleId(), message.getAuthorId());
    }

    @Override
    public void handleLikeMessage(LikeRabbitMessage message) {

    }

    @Override
    public void handleCollectMessage(CollectRabbitMessage message) {

    }

    @Override
    public void handleFollowMessage(FollowRabbitMessage message) {

    }

    @Override
    public void handleCommentMessage(CommentRabbitMessage message) {
        articleStatService.increaseComments(message.getArticleId(), 1);
    }

    @Override
    public void handleReplyMessage(ReplyRabbitMessage message) {
        articleStatService.increaseComments(message.getArticleId(), 1);
    }

    @Override
    public void handleCommentDeleteMessage(CommentDeletedRabbitMessage message) {
        articleStatService.decreaseComments(message.getArticleId(), message.getDeletedCount());
    }
}
