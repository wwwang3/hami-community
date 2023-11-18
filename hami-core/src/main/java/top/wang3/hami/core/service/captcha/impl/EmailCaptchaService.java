package top.wang3.hami.core.service.captcha.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.dto.captcha.*;
import top.wang3.hami.common.util.RandomUtils;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.exception.CaptchaServiceException;
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
    private static final String PREFIX = "email:";

    @Override
    public void sendCaptcha(CaptchaType type, String item, int length, long expire) {
        //发送验证码
        String value = RandomUtils.randomIntStr(length);
        //将验证码存储在Redis
        String captchaKey = buildKey(type, item);
        //保存在redis
        boolean success = RedisClient.setNx(captchaKey, value, expire, TimeUnit.SECONDS);
        if (!success) {
            throw new CaptchaServiceException("请求频繁, 请稍后再试");
        }
        EmailCaptcha captcha = buildCaptchaMessage(type, item, value, expire);
        rabbitMessagePublisher.publishMsg(captcha);
    }

    @Override
    public boolean verify(CaptchaType type, String item, String value) {
        String key = buildKey(type, item);
        String captcha = RedisClient.getCacheObject(key);
        return captcha != null && captcha.equals(value);
    }

    @Override
    public void deleteCaptcha(CaptchaType type, String item) {
        RedisClient.deleteObject(buildKey(type, item));
    }

    private EmailCaptcha buildCaptchaMessage(CaptchaType type, String item, String value, long expire) {
        return switch (type) {
            case REGISTER -> new RegisterEmailCaptcha(item, value, expire);
            case RESET_PASS -> new ResetPassEmailCaptcha(item, value, expire);
            case UPDATE_PASS -> new UpdatePassEmailCaptcha(item, value, expire);
        };
    }

    private String buildKey(CaptchaType type, String item) {
        return PREFIX + type.toString() + ":" + item;
    }

}
