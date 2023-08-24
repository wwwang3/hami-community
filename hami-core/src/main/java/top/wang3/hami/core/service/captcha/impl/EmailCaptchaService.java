package top.wang3.hami.core.service.captcha.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.Captcha;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.exception.CaptchaServiceException;
import top.wang3.hami.core.service.captcha.CaptchaService;
import top.wang3.hami.security.context.IpContext;

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
    public void sendCaptcha(Captcha captcha) throws CaptchaServiceException {
        String ip = IpContext.getIpInfo().getIp();
        //对IP进行加锁, 找不到IP的为unknown, 全部都要排队
        synchronized (ip.intern()) {
            //检查是否限流
            if (checkLimited()) {
                throw new CaptchaServiceException("请求频繁");
            }
            //发送验证码
            rabbitTemplate.convertAndSend(Constants.EMAIL_EXCHANGE, Constants.EMAIL_ROUTING,
                    captcha);
            //将验证码存储在Redis
            String captchaKey = captcha.getType() + captcha.getItem();
            //保存在redis
            RedisClient.setCacheObject(captchaKey, captcha.getValue(), captcha.getExpire());
        }
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


    /**
     * 检查请求是否被限制
     * @return true-被限制 false-没有限制
     */
    private boolean checkLimited() {
        String key = limitKey();
        if (RedisClient.exist(key)) {
            //存在, 表示受到限制
            return true;
        } else {
            //设置60s
            RedisClient.setCacheObject(key, "", 60);
            return false;
        }
    }

    public static String resolveCaptchaType(String type) {
        return switch (type) {
            case "register" -> Constants.REGISTER_EMAIL_CAPTCHA;
            case "reset" -> Constants.RESET_EMAIL_CAPTCHA;
            case "update" -> Constants.UPDATE_EMAIL_CAPTCHA;
            default -> throw new IllegalArgumentException(type);
        };
    }


    private String limitKey() {
        return Constants.CAPTCHA_RATE_LIMIT + IpContext.getIpInfo().getIp();
    }
}
