package top.wang3.hami.core.service.article.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.message.ReviewSuccessMessage;
import top.wang3.hami.common.message.email.AlarmEmailMessage;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.service.article.ArticleDraftService;
import top.wang3.hami.core.service.mail.MailMessageHandler;
import top.wang3.hami.security.model.Result;

@Component
@RabbitListener(bindings = {
    @QueueBinding(
        value = @Queue("hami-article-queue-2"),
        exchange = @Exchange(value = RabbitConstants.HAMI_TOPIC_EXCHANGE1, type = "topic"),
        key = {"content.review.success"}
    ),
}, concurrency = "2")
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("all")
public class ArticleReviewMessageConsumer {

    private final ArticleDraftService articleDraftService;
    private final RabbitMessagePublisher rabbitMessagePublisher;
    private final MailMessageHandler mailMessageHandler;

    @RabbitHandler
    public void handleReviewSuccessMessage(ReviewSuccessMessage message) {
        try {
            articleDraftService.publishArticle(message.draftId());
        } catch (Exception e) {
            e.printStackTrace();
            String msgs = Result.writeValueAsString(message);
            AlarmEmailMessage msg = new AlarmEmailMessage("审核成功后发表文章失败", msgs);
            mailMessageHandler.handle(msg);
        }
    }
}
