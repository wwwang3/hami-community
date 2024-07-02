package top.wang3.hami.core.service.admin.consumer;


import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.message.ReviewMessage;
import top.wang3.hami.core.HamiProperties;
import top.wang3.hami.mail.service.MailSenderService;

import java.util.List;


@Component
@RabbitListener(bindings = {
    @QueueBinding(
        value = @Queue("hami-admin-queue-1"),
        exchange = @Exchange(value = RabbitConstants.HAMI_TOPIC_EXCHANGE1, type = "topic"),
        key = {"content.review.need"}
    )
},
    containerFactory = RabbitConstants.BATCH_LISTENER_FACTORY
)
@RequiredArgsConstructor
public class ReviewMessageConsumer {

    private final MailSenderService mailSenderService;
    private final HamiProperties hamiProperties;

    @RabbitHandler
    public void handleReviewMessage(List<ReviewMessage> messages) {
        try {
            String email = hamiProperties.getEmail();
            String msg = "近期有%s个草稿需要发布, 快去审核吧!".formatted(messages.size());
            mailSenderService.sendText("内容审核通知", msg, List.of(email));
        } catch (Exception e) {
            e.printStackTrace();
            // ignore it
        }
    }
}
