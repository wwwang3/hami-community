package top.wang3.hami.core.service.captcha.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.dto.Captcha;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.exception.CaptchaServiceException;
import top.wang3.hami.core.exception.ServiceException;
import top.wang3.hami.core.service.captcha.CaptchaService;

import java.util.concurrent.TimeUnit;

/**
 * 邮箱验证码服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailCaptchaService implements CaptchaService {

    private final RabbitMessagePublisher rabbitMessagePublisher;

    @Override
    public void sendCaptcha(Captcha captcha) throws CaptchaServiceException {
        //发送验证码
        //将验证码存储在Redis
        String captchaKey = captcha.getType() + captcha.getItem();
        //保存在redis
        boolean success = RedisClient.setNx(captchaKey, captcha.getValue(), captcha.getExpire(), TimeUnit.SECONDS);
        if (!success) {
            throw new ServiceException("请求频繁, 请稍后再试");
        }
        rabbitMessagePublisher.publishMsg(captcha);
    }

    @Override
    public boolean verify(String type, String item, String value) {
        String key = type + item;
        String captcha = RedisClient.getCacheObject(key);
        return captcha != null && captcha.equals(value);
    }

    @Override
    public void deleteCaptcha(String type, String item) {
        RedisClient.deleteObject(type + item);
    }

}
