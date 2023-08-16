package top.wang3.hami.core.service.captcha;

import top.wang3.hami.common.dto.Captcha;

/**
 * 验证码服务接口
 */
public interface CaptchaService {

    /**
     * 发送验证码
     * @param captcha Captcha
     */
    void sendCaptcha(Captcha captcha);

    boolean verify(String type, String item, String value);
}
