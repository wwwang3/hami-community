package top.wang3.hami.common.constant;

public final class Constants {

    public static final String EMPTY_STRING = "";


    public static final String HAMI_DIRECT_EXCHANGE1 = "hami-direct-exchange-1";

    public static final String HAMI_TOPIC_EXCHANGE1 = "hami-topic-exchange-1";

    public static final String HAMI_TOPIC_EXCHANGE2 = "hami-topic-exchange-2";

    public static final String EMAIL_QUEUE = "hami-email-queue";

    public static final String EMAIL_ROUTING = "email";

    public static final String NOTIFY_ROUTING = "/notify-msg";

    public static final String CANAL_EXCHANGE = "hami-canal";

    public static final String CANAL_QUEUE = "hami-canal-queue";

    public static final String CANAL_ROUTING = "/canal";

    /**
     * 注册邮箱验证码
     */
    public static final String REGISTER_EMAIL_CAPTCHA = "email:register:";

    /**
     * 忘记密码邮箱验证码
     */
    public static final String RESET_EMAIL_CAPTCHA = "email:reset:pass:";

    /**
     * 更新密码邮箱验证码
     */
    public static final String UPDATE_EMAIL_CAPTCHA = "email:update:pass:";

    public static final Byte TWO =  2;

    public static final Byte ONE = 1;

    public static final Byte ZERO = 0;

    public static final Byte DELETED = ONE;
    public static final Byte NOT_DELETED = ZERO;

    /**
     * 点赞类型
     */
    public static final Byte LIKE_TYPE_ARTICLE = 1;
    public static final Byte LIKE_TYPE_COMMENT = 2;

    /**
     * 用户点赞数(我点赞的文章/评论数)
     */
    public static final String USER_LIKE_COUNT = "user:like:count:";

    /**
     * 用户关注数
     */
    public static final String USER_FOLLOWING_COUNT = "user:following:count:";

    /**
     * 用户粉丝数
     */
    public static final String USER_FOLLOWER_COUNT = "user:follower:count:";

    /**
     * 用户收藏数(我收藏的文章数量)
     */
    public static final String USER_COLLECT_COUNT = "user:collect:count:";

    /**
     * 用户文章数量
     */
    public static final String USER_ARTICLE_COUNT = "user:article:count:";


    public static final String CaffeineCacheManager = "caffeineCacheManager";

    public static final String RedisCacheManager = "redisCacheManager";

    public static final String REDIS_CACHE_NAME = "CACHE_REDIS";
    public static final String CAFFEINE_CACHE_NAME = "CACHE_LOCAL";

    /**
     * 计数服务常量
     */
    public static final String COUNT_ARTICLE_STAT = "article:stat:";
    public static final String ARTICLE_LIKES = "likes";
    public static final String ARTICLE_COLLECTS = "collects";
    public static final String ARTICLE_COMMENTS = "comments";
    public static final String ARTICLE_VIEWS = "views";

    /**
     * 用户文章收到的总点赞数
     */
    public static final String USER_TOTAL_LIKES = "total_likes";

    /**
     * 用户文章收到的总评论数
     */
    public static final String USER_TOTAL_COMMENTS = "total_comments";

    /**
     * 用户文章收到的总收藏数
     */
    public static final String USER_TOTAL_COLLECTS = "total_collects";

    /**
     * 用户总关注数
     */
    public static final String USER_TOTAL_FOLLOWINGS = "total_followings";

    /**
     * 用户总粉丝数
     */
    public static final String USER_TOTAL_FOLLOWERS = "total_followers";

    /**
     * 用户总文章数
     */
    public static final String USER_TOTAL_ARTICLES = "total_articles";

    /**
     * 用户文章的总阅读量
     */
    public static final String USER_TOTAL_VIEWS = "total_views";

    public static final String COUNT_TYPE_ARTICLE = "#count:article:";
    public static final String COUNT_TYPE_USER = "#count:user:";

    public static final String LIST_USER_FOLLOWING = "user:following:list:";

    public static final String LIST_USER_FOLLOWER = "user:follower:list:";

    public static final String LIST_USER_COLLECT = "user:collect:list:";

    public static final String LIST_USER_LIKE = "user:like:list:";

    public static final String LIST_USER_LIKE_ARTICLES = "user:like:list:" + LIKE_TYPE_ARTICLE + ":";

    public static final String LIST_USER_ARTICLE = "user:article:list:";

    public static final String ARTICLE_LIST = "article:list:total";

    public static final String CATE_ARTICLE_LIST = "cate:article:list:";
    public static final String OVERALL_HOT_ARTICLES = "rank:article:overall";
    public static final String HOT_ARTICLE = "rank:article:";

    public static final String ARTICLE_INFO = "#article:info:";
    public static final String USER_INFO = "#user:info:";

    public static final String ARTICLE_CONTENT = "#article:content:";
}
