package top.wang3.hami.core.service.captcha.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.dto.Captcha;
import top.wang3.hami.core.exception.CaptchaServiceException;
import top.wang3.hami.core.service.captcha.CaptchaService;

/**
 * 邮箱验证码服务实现
 */
@Service
@Slf4j
public class EmailCaptchaService implements CaptchaService {

    @Override
    public void sendCaptcha(Captcha captcha) throws CaptchaServiceException {
        //发送验证码
        //向消息队列发送消息
        //将验证码存储在Redis
    }

    @Override
    public boolean verify(String type, String item, String value) {
        return false;
    }
}
