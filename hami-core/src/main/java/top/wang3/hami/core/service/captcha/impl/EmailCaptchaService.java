package top.wang3.hami.core.service.captcha.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.Captcha;
import top.wang3.hami.core.service.captcha.CaptchaService;

/**
 * 邮箱验证码服务实现
 */
@Service
@Slf4j
public class EmailCaptchaService implements CaptchaService {

    private final RabbitTemplate rabbitTemplate;

    public EmailCaptchaService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void sendCaptcha(Captcha captcha) {
        //发送验证码
        //向消息队列发送消息
        //将验证码存储在Redis

        rabbitTemplate.convertAndSend(Constants.EMAIL_EXCHANGE, Constants.EMAIL_ROUTING,
                captcha);
    }

    @Override
    public boolean verify(String type, String item, String value) {
        return false;
    }

}
