package top.wang3.hami.core.service.mail.consumer;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.message.email.MailMessage;
import top.wang3.hami.core.service.mail.MailMessageHandler;
import top.wang3.hami.mail.model.MailSendResult;

@Component
@RabbitListener(
        bindings = {
                @QueueBinding(
                        value = @Queue(value = RabbitConstants.EMAIL_QUEUE),
                        exchange = @Exchange(value = RabbitConstants.HAMI_EMAIL_EXCHANGE),
                        key = {RabbitConstants.EMAIL_ROUTING}
                )
        },
        concurrency = "4"
)
@RequiredArgsConstructor
@Slf4j
public class EmailQueueListener {

    private final MailMessageHandler mailMessageHandler;


    @PostConstruct
    public void init() {
        log.debug("rabbit listener EmailQueueListener register for use");
    }

    @RabbitHandler
    public void handleEmailMessage(MailMessage message) {
        try {
            MailSendResult result = mailMessageHandler.handle(message);
            log.info("send result: {}", result);
        } catch (Exception e) {
            // ignore it
        }
    }
}
