package top.wang3.hami.common.constant;

public final class Constants {

    public static final String EMAIL_EXCHANGE = "hami-email-exchange";
    public static final String EMAIL_QUEUE = "hami-email-queue";

    public static final String EMAIL_ROUTING = "email";

    public static final String NOTIFY_EXCHANGE = "hami-notify-exchange";

    public static final String NOTIFY_QUEUE = "hami-notify-queue";

    public static final String NOTIFY_ROUTING = "/notify-msg";

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
    public static final Integer LIKE_TYPE_ARTICLE = 1;
    public static final Integer LIKE_TYPE_COMMENT = 2;

    /**
     * 通知类型: 0-系统，1-评论，2-回复 3-点赞 4-收藏 5-关注 6-关注的用户发布新文章
     */
    public static final int NOTIFY_TYPE_SYSTEM = 0;
    public static final int NOTIFY_TYPE_COMMENT = 1;
    public static final int NOTIFY_TYPE_REPLY = 2;
    public static final int NOTIFY_TYPE_LIKE = 3;
    public static final int NOTIFY_TYPE_COLLECT = 4;
    public static final int NOTIFY_TYPE_FOLLOW = 5;
    public static final int NOTIFY_TYPE_NEW_ARTICLE = 6;
}
