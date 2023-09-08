package top.wang3.hami.message.listener;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.Captcha;
import top.wang3.hami.mail.model.MailSendResult;
import top.wang3.hami.mail.service.MailSenderService;

import java.util.concurrent.TimeUnit;

@Component
@RabbitListener(messageConverter = "#{rabbitMQJacksonConverter}",
        queues = Constants.EMAIL_QUEUE, concurrency = "2")
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
        try {
            if (captcha == null || !StringUtils.hasText(captcha.getItem())) {
                log.debug("empty email");
                return;
            }
            log.info("captcha: {}", captcha);
            String type = captcha.getType();
            String subject = getSubject(type);
            String template = getTemplate(captcha);
            MailSendResult result = mailSenderService.sendText(subject, template, captcha.getItem());
            log.debug("send result: {}", result);
        } catch (Exception e) {
            //防止发送出现异常无限消费
            //发送失败要用户重试
            log.warn("failed to send email, error_class: {}, error_msg: {}", e.getClass(), e.getMessage());
        }
    }

    private String getTemplate(Captcha captcha) {
        if (Constants.REGISTER_EMAIL_CAPTCHA.equals(captcha.getType())) {
            return REGISTER_TEMPLATE.formatted(captcha.getValue(),
                    TimeUnit.SECONDS.toMinutes(captcha.getExpire()));
        } else if (Constants.RESET_EMAIL_CAPTCHA.equals(captcha.getType())) {
            return RESET_TEMPLATE.formatted(captcha.getValue(),
                    TimeUnit.SECONDS.toMinutes(captcha.getExpire()));
        } else {
            throw new UnsupportedOperationException("unsupported type :" + captcha.getType());
        }
    }

    private String getSubject(String type) {
        if (Constants.REGISTER_EMAIL_CAPTCHA.equals(type)) {
            return "注册验证码";
        } else if (Constants.RESET_EMAIL_CAPTCHA.equals(type)) {
            return "重置密码验证码";
        } else {
            throw new UnsupportedOperationException("unsupported type :" + type);
        }
    }
}
