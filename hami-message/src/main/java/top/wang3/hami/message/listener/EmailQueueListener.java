package top.wang3.hami.message.listener;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.Captcha;
import top.wang3.hami.mail.model.MailSendResult;
import top.wang3.hami.mail.service.MailSenderService;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@RabbitListener(messageConverter = "rabbitMQJacksonConverter", queues = Constants.EMAIL_QUEUE)
@Slf4j
public class EmailQueueListener {

    private final MailSenderService mailSenderService;

    public static final String REGISTER_TEMPLATE = "欢迎注册Hami社区!%n您的验证码为: %s 有效期%s分钟, 请勿泄露~";

    public static final String RESET_TEMPLATE = "你正在重置密码!%n您的验证码为: %s 有效期%s分钟, 请勿泄露, 如不是本人操作请忽略!";


    public EmailQueueListener(MailSenderService mailSenderService) {
        this.mailSenderService = mailSenderService;
    }

    @PostConstruct
    public void init() {
        log.debug("rabbit listener EmailQueueListener register for use");
    }


    @RabbitHandler
    public void sendMailMessage(Captcha captcha) {
        if (captcha == null) return;
        log.info("captcha: {}", captcha);
        String type = captcha.getType();
        MailSendResult result = switch (type) {
            case Constants.REGISTER_EMAIL_CAPTCHA -> mailSenderService.of()
                    .subject("注册验证码")
                    .text(String.format(REGISTER_TEMPLATE, captcha.getValue(),
                            TimeUnit.SECONDS.toMinutes(captcha.getExpire())))
                    .to(captcha.getItem())
                    .date(new Date())
                    .send();
            case Constants.RESET_EMAIL_CAPTCHA -> mailSenderService.of()
                    .subject("重置密码")
                    .text(String.format(RESET_TEMPLATE, captcha.getValue(),
                            TimeUnit.SECONDS.toMinutes(captcha.getExpire())))
                    .to(captcha.getItem())
                    .date(new Date())
                    .send();
            default -> throw new UnsupportedOperationException("Unsupported value: " + type);
        };
        log.debug("send result: {}", result);
    }
}
