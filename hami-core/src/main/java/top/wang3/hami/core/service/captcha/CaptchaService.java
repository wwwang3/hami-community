package top.wang3.hami.core.service.captcha;

import top.wang3.hami.common.dto.captcha.Captcha;
import top.wang3.hami.common.dto.captcha.CaptchaType;
import top.wang3.hami.core.exception.CaptchaServiceException;

/**
 * 验证码服务接口
 */
public interface CaptchaService {

    /**
     * 发送验证码
     * @param type 类型
     * @param item 接收验证码的主体
     * @param length 长度
     * @param expire 有效期单位s
     */
     void sendCaptcha(CaptchaType type, String item, int length, long expire) throws CaptchaServiceException;


    /**
     * 发送验证码 默认长度6位, 有效期五分钟
     * @param type 类型
     * @param item 主体
     */
    default void sendCaptcha(CaptchaType type, String item) throws CaptchaServiceException {
        sendCaptcha(type, item, 6, Captcha.DEFAULT_EXPIRE);
    }

    /**
     * 校验验证码和redis的是否一致
     * @param type 类型
     * @param item 主体
     * @param value 验证码
     * @return true-匹配 false-不匹配或者过期了
     */
    boolean verify(CaptchaType type, String item, String value);

    void deleteCaptcha(CaptchaType type, String item);
}
