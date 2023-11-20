package top.wang3.hami.common.constant;

public final class Constants {

    public static final String EMPTY_STRING = "";

    public static final String Hi_PRE_TAG = "<em>";
    public static final String Hi_POST_TAG = "</em>";


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

    public static final String REQ_ID = "reqId";
}
