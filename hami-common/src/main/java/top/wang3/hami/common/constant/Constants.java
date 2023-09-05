package top.wang3.hami.common.constant;

public final class Constants {

    public static final String EMAIL_EXCHANGE = "hami-email-exchange";
    public static final String EMAIL_QUEUE = "hami-email-queue";

    public static final String EMAIL_ROUTING = "email";

    public static final String NOTIFY_EXCHANGE = "hami-notify-exchange";

    public static final String NOTIFY_QUEUE = "hami-notify-queue";

    public static final String NOTIFY_ROUTING = "/notify-msg";

    public static final String CANAL_EXCHANGE = "hami-canal";

    public static final String CANAL_QUEUE = "hami-canal-queue";

    public static final String CANAL_ROUTING = "/canal";

    /**
     * 注册邮箱验证码
     */
    public static final String REGISTER_EMAIL_CAPTCHA = "email:register";

    /**
     * 忘记密码邮箱验证码
     */
    public static final String RESET_EMAIL_CAPTCHA = "email:reset:pass";

    /**
     * 更新密码邮箱验证码
     */
    public static final String UPDATE_EMAIL_CAPTCHA = "email:reset:pass";

    public static final String CAPTCHA_RATE_LIMIT = "captcha:rate:limit";

    public static final Byte ONE = (byte) 1;

    public static final Byte ZERO = (byte) 0;

    public static final Byte DELETED = ONE;
    public static final Byte NOT_DELETED = ZERO;

    /**
     * 点赞类型
     */
    public static final Byte LIKE_TYPE_ARTICLE = 1;
    public static final Byte LIKE_TYPE_COMMENT = 2;


}
