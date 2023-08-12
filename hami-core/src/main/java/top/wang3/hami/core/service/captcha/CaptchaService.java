package top.wang3.hami.core.service.captcha;

import top.wang3.hami.core.exception.CaptchaServiceException;

/**
 * 验证码服务接口
 */
public interface CaptchaService {

    /**
     * 发送验证码
     * @param captcha Captcha
     * @throws CaptchaServiceException e
     */
    void sendCaptcha(Captcha captcha) throws CaptchaServiceException;

    boolean verify(String type, String item, String captcha);
}
