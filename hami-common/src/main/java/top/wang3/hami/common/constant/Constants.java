package top.wang3.hami.common.constant;

public final class Constants {


    public static final String HAMI_DIRECT_EXCHANGE1 = "hami-direct-exchange-1";

    public static final String HAMI_DIRECT_EXCHANGE2 = "hami-direct-exchange-2";
    public static final String EMAIL_QUEUE = "hami-email-queue";

    public static final String EMAIL_ROUTING = "email";

    public static final String NOTIFY_QUEUE = "hami-notify-queue";

    public static final String NOTIFY_ROUTING = "/notify-msg";

    public static final String CANAL_EXCHANGE = "hami-canal";

    public static final String CANAL_QUEUE = "hami-canal-queue";

    public static final String CANAL_ROUTING = "/canal";
    public static final String ADD_VIEWS_QUEUE = "hami-add-views-queue";
    public static final String READING_RECORD_QUEUE = "hami-reading-record-queue";

    public static final String ADD_VIEWS_ROUTING = "/views";

    public static final String READING_RECORD_ROUTING = "/reading-record";

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

    public static final String CaffeineCacheManager = "caffeineCacheManager";

    public static final String RedisCacheManager = "redisCacheManager";

    public static final String REDIS_CACHE_NAME = "HAMI_CACHE_REDIS_";
    public static final String CAFFEINE_CACHE_NAME = "HAMI_CACHE_LOCAL_";

    /**
     * 计数服务常量
     */
    public static final String COUNT_ARTICLE_STAT = "article:stat:";
    public static final String ARTICLE_LIKES = "likes";
    public static final String ARTICLE_COLLECTS = "collects";
    public static final String ARTICLE_COMMENTS = "comments";
    public static final String ARTICLE_VIEWS = "views";

    public static final String USER_TOTAL_LIKES = "total_likes";
    public static final String USER_TOTAL_COMMENTS = "total_comments";
    public static final String USER_TOTAL_COLLECTS = "total_collects";
    public static final String USER_TOTAL_FOLLOWINGS = "total_followings";
    public static final String USER_TOTAL_FOLLOWERS = "total_followers";
    public static final String USER_TOTAL_ARTICLES = "total_articles";
    public static final String USER_TOTAL_VIEWS = "total_views";

    public static final String COUNT_TYPE_ARTICLE = "#article:";
    public static final String COUNT_TYPE_USER = "#user:";

    public static final String LIST_USER_FOLLOWING = "user:following:list:";

    public static final String LIST_USER_FOLLOWER = "user:follower:list:";

    public static final String LIST_USER_COLLECT = "user:collect:list:";

    public static final String LIST_USER_LIKE = "user:like:list:";

    public static final String OVERALL_HOT_ARTICLES = "rank:article:overall";
    public static final String HOT_ARTICLE = "rank:article:";
}
