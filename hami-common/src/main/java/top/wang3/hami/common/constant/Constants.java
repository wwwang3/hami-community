package top.wang3.hami.common.constant;

public final class Constants {

    public static final String EMAIL_EXCHANGE = "hami-email-exchange";
    public static final String EMAIL_QUEUE = "hami-email-queue";

    public static final String EMAIL_ROUTING = "email";

    /**
     * 注册邮箱验证码
     */
    public static final String REGISTER_EMAIL_CAPTCHA = "email:register";

    /**
     * 重置密码邮箱验证码
     */
    public static final String RESET_EMAIL_CAPTCHA = "email:reset:pass";

    public static final String CAPTCHA_RATE_LIMIT = "captcha:rate:limit";

    public static final Byte ONE = (byte) 1;

    public static final Byte ZERO = (byte) 0;
}
