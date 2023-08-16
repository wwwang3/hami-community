package top.wang3.hami.core.service.captcha;

import top.wang3.hami.common.dto.Captcha;
import top.wang3.hami.common.util.RandomUtils;
import top.wang3.hami.core.exception.CaptchaServiceException;

import java.util.concurrent.TimeUnit;

/**
 * 验证码服务接口
 */
public interface CaptchaService {

    /**
     * todo 完善限流
     * 发送验证码, 会对IP进行限制, 默认60秒内只能调用一次
     * @param captcha Captcha
     */
    void sendCaptcha(Captcha captcha) throws CaptchaServiceException;

    /**
     * 发送验证码
     * @param type 类型
     * @param item 接收验证码的主体
     * @param length 长度
     * @param expire 有效期单位s
     */
    default void sendCaptcha(String type, String item, int length, long expire) throws CaptchaServiceException {
        String value = Integer.toString(RandomUtils.getRandom(length));
        Captcha captcha = new Captcha(type, item, value, expire);
        sendCaptcha(captcha);
    }

    /**
     * 发送验证码 默认长度6位, 有效期五分钟
     * @param type 类型
     * @param item 主体
     */
    default void sendCaptcha(String type, String item) throws CaptchaServiceException {
        sendCaptcha(type, item, 6, TimeUnit.MINUTES.toSeconds(5));
    }

    /**
     * 校验验证码和redis的是否一致
     * @param type 类型
     * @param item 主体
     * @param value 验证码
     * @return true-匹配 false-不匹配或者过期了
     */
    boolean verify(String type, String item, String value);

    void deleteCaptcha(String type, String item);
}
