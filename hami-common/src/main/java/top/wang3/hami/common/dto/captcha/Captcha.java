package top.wang3.hami.common.dto.captcha;

public interface Captcha {

    long DEFAULT_EXPIRE  = 300L;

    /**
     * 类型 作为redis-key前缀
     */
    CaptchaType type();

    /**
     * 接收验证码的主体
     */
    String item();

    /**
     * 验证码
     */
    String value();

    /**
     * 有效期 单位s
     */
    long expire();

}
