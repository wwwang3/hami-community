package top.wang3.hami.common.constant;

import java.util.concurrent.TimeUnit;

public class TimeoutConstants {

    /**
     * 默认过期时间
     */
    public static final long DEFAULT_EXPIRE = TimeUnit.DAYS.toMillis(1);

    public static final long ARTICLE_COUNT_EXPIRE = TimeUnit.DAYS.toMillis(10);

    public static final long ARTICLE_LIST_EXPIRE = TimeUnit.DAYS.toMillis(20);

    public static final long USER_ARTICLE_LIST_EXPIRE = TimeUnit.HOURS.toHours(24);

    public static final long ARTICLE_STAT_EXPIRE = TimeUnit.DAYS.toMillis(4);

    public static final long USER_STAT_EXPIRE = TimeUnit.DAYS.toMillis(2);

    public static final long USER_INFO_EXPIRE = TimeUnit.DAYS.toMillis(6);

    public static final long ARTICLE_INFO_EXPIRE = TimeUnit.DAYS.toMillis(8);

    public static final long FOLLOWER_LIST_EXPIRE = TimeUnit.DAYS.toMillis(7);

    public static final long FOLLOWING_LIST_EXPIRE = TimeUnit.DAYS.toMillis(8);

    public static final long COLLECT_LIST_EXPIRE = TimeUnit.DAYS.toMillis(9);

    public static final long LIKE_LIST_EXPIRE = TimeUnit.DAYS.toMillis(10);

    public static final long INTERACT_COUNT_EXPIRE = TimeUnit.DAYS.toMillis(10);

}
