package top.wang3.hami.core.service.interact.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.dto.interact.LikeType;
import top.wang3.hami.common.message.ArticleRabbitMessage;
import top.wang3.hami.core.service.interact.repository.CollectRepository;
import top.wang3.hami.core.service.interact.repository.LikeRepository;
import top.wang3.hami.core.service.interact.repository.ReadingRecordRepository;


@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleInteractMessageConsumer {

    private final LikeRepository likeRepository;
    private final CollectRepository collectRepository;
    private final ReadingRecordRepository readingRecordRepository;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "hami-user-interact-queue-2"),
                    exchange = @Exchange(value = RabbitConstants.HAMI_TOPIC_EXCHANGE2, type = "topic"),
                    key = {"article.delete"}
            )
    )
    public void handleArticleMessageForLike(ArticleRabbitMessage message) {
        //文章删除消息
        int deleted = likeRepository.deleteLikeItem(message.getArticleId(), LikeType.ARTICLE);
        log.info("article deleted, async to delete like-item, deleted-count: {}", deleted);
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "hami-user-interact-queue-3"),
                    exchange = @Exchange(value = RabbitConstants.HAMI_TOPIC_EXCHANGE2, type = "topic"),
                    key = {"article.delete"}
            )
    )
    public void handleArticleMessageForCollect(ArticleRabbitMessage message) {
        //文章删除消息
        int deleted = collectRepository.deleteCollectItem(message.getArticleId());
        log.info("article deleted, async to delete collect-item, deleted-count: {}", deleted);
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "hami-user-interact-queue-4"),
                    exchange = @Exchange(value = RabbitConstants.HAMI_TOPIC_EXCHANGE2, type = "topic"),
                    key = {"article.view"}
            ),
            concurrency = "4"
    )
    public void handleArticleDeleteMessage(ArticleRabbitMessage message) {
        if (message.getLoginUserId() != null) {
            readingRecordRepository.record(message.getLoginUserId(), message.getArticleId());
        }
    }
}
