package top.wang3.hami.common.constant;

public final class Constants {

    public static final String Hi_PRE_TAG = "<em>";
    public static final String Hi_POST_TAG = "</em>";

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

    public static final String CaffeineCacheManager = "caffeineCacheManager";

    public static final String RedisCacheManager = "redisCacheManager";

    public static final String REDIS_CACHE_NAME = "CACHE_REDIS";
    public static final String CAFFEINE_CACHE_NAME = "CACHE_LOCAL";


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

    public static final String LIST_USER_FOLLOWING = "user:following:list:";

    public static final String LIST_USER_FOLLOWER = "user:follower:list:";

    public static final String LIST_USER_COLLECT = "user:collect:list:";

    public static final String LIST_USER_LIKE = "user:like:list:";

    public static final String LIST_USER_LIKE_ARTICLES = "user:like:list:" + LIKE_TYPE_ARTICLE + ":";

    public static final String LIST_USER_ARTICLE = "user:article:list:";

    public static final String ARTICLE_LIST = "article:list:total";
    public static final String CATE_ARTICLE_LIST = "cate:article:list:";

    public static final String TOTAL_ARTICLE_COUNT = "article:count:total";

    public static final String CATE_ARTICLE_COUNT = "article:count:";
    public static final String OVERALL_HOT_ARTICLES = "rank:article:overall";
    public static final String HOT_ARTICLE = "rank:article:";

    public static final String ARTICLE_INFO = "#article:info:";
    public static final String USER_INFO = "#user:info:";

    public static final String ARTICLE_CONTENT = "#article:content:";

    public static final String STAT_TYPE_ARTICLE = "#stat:article:";
    public static final String STAT_TYPE_USER = "#stat:user:";

    public static final String USER_STAT_ARTICLES = "user:total:articles";
    public static final String USER_STAT_VIEWS = "user:received:views";
    public static final String USER_STAT_LIKES = "user:received:likes";
    public static final String USER_STAT_COMMENTS = "user:received:comments";
    public static final String USER_STAT_COLLECTS = "user:received:collects";


    public static final String DATA_GROWING = "data:growing:";

    /**
     * 文章数增量
     */
    public static final String DATA_GROWING_ARTICLE = "article_incr";

    /**
     * 阅读数增量
     */
    public static final String DATA_GROWING_VIEW = "view_incr";

    /**
     * 点赞增量
     */
    public static final String DATA_GROWING_LIKE = "like_incr";

    /**
     * 评论数增量
     */
    public static final String DATA_GROWING_COMMENT = "comment_incr";

    /**
     * 文章收藏数增量
     */

    public static final String DATA_GROWING_COLLECT = "collect_incr";

    /**
     * 粉丝增量
     */
    public static final String DATA_GROWING_FOLLOWER = "follower_incr";

    /**
     * 取消关注增量(掉粉量)
     */
    public static final String DATA_GROWING_CANCEL_FOLLOW = "cancel_follow_incr";
    public static final String VIEW_LIMIT = "view:limit:";
    public static final String HOT_SEARCH = "hot:search:list";
}
