package top.wang3.hami.message.listener;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.Captcha;
import top.wang3.hami.mail.service.MailSenderService;

@Component
@RabbitListener(messageConverter = "rabbitMQJacksonConverter", queues = Constants.EMAIL_QUEUE)
@Slf4j
public class EmailQueueListener {

    private final MailSenderService mailSenderService;


    public EmailQueueListener(MailSenderService mailSenderService) {
        this.mailSenderService = mailSenderService;
    }

    @RabbitHandler
    public void sendMailMessage(Captcha captcha) {
        log.debug("receive {}", captcha);
    }

    @PostConstruct
    public void init() {
        log.debug("rabbit listener EmailQueueListener registered for use");
    }
}
