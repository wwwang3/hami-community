package top.wang3.hami.core.service.captcha;

import org.springframework.data.redis.core.StringRedisTemplate;
import top.wang3.hami.core.exception.CaptchaServiceException;

/**
 * 邮箱验证码服务实现
 */
public class EmailCaptchaService implements CaptchaService {


    private final StringRedisTemplate redisTemplate;

    public EmailCaptchaService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    @Override
    public void sendCaptcha(Captcha captcha) throws CaptchaServiceException {
        //发送验证码
        //向消息队列发送消息
        //将验证码存储在Redis
    }

    @Override
    public boolean verify(String type, String item, String captcha) {
        return false;
    }
}
