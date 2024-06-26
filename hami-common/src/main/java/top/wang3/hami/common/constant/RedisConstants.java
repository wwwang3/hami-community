package top.wang3.hami.common.constant;

/**
 * Redis key-prefix常量
 */
public final class RedisConstants {

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

    public static final String USER_FOLLOWING_LIST = "user:following:list:";

    public static final String USER_FOLLOWER_LIST = "user:follower:list:";

    public static final String USER_COLLECT_LIST = "user:collect:list:";

    public static final String USER_LIKE_LIST = "user:like:list:";

    public static final String USER_ARTICLE_LIST = "user:article:list:";

    public static final String ARTICLE_LIST = "article:list:total";
    public static final String CATE_ARTICLE_LIST = "cate:article:list:";

    public static final String USER_INTERACT_COUNT_HASH = "interact:count:hash:";

    public static final String LIKE_INTERACT_HKEY = "like:";

    public static final String LIKE_ARTICLE_INTERACT_HKEY = LIKE_INTERACT_HKEY + Constants.LIKE_TYPE_ARTICLE;

    public static final String COLLECT_INTERACT_HKEY = "collect";
    public static final String FOLLOW_INTERACT_HKEY = "follow";
    public static final String FOLLOWER_INTERACT_HKEY = "follower";

    /**
     * 文章总数和分类文章数
     */
    public static final String ARTICLE_COUNT_HASH = "article:count:map";

    public static final String TOTAL_ARTICLE_COUNT_HKEY = "total";

    public static final String CATE_ARTICLE_COUNT_HKEY = "cate:";

    public static final String OVERALL_HOT_ARTICLE = "rank:article:overall";

    public static final String CATE_HOT_ARTICLE = "rank:article:";

    public static final String AUTHOR_RANKING = "rank:author";

    public static final String ARTICLE_INFO = "#article:info:";
    public static final String USER_INFO = "#user:info:";

    public static final String ACCOUNT_INFO = "#account:info:";

    public static final String ARTICLE_CONTENT = "#article:content:";

    public static final String STAT_TYPE_ARTICLE = "#stat:article:";
    public static final String STAT_TYPE_USER = "#stat:user:";

    public static final String USER_STAT_ID = "user_id";
    public static final String USER_STAT_ARTICLES = "totalArticles";
    public static final String USER_STAT_VIEWS = "totalViews";
    public static final String USER_STAT_LIKES = "totalLikes";
    public static final String USER_STAT_COMMENTS = "totalComments";
    public static final String USER_STAT_COLLECTS = "totalCollects";
    public static final String USER_STAT_FOLLOWERS = "totalFollowers";
    public static final String USER_STAT_FOLLOWINGS = "totalFollowings";


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

    public static final String UN_READ_NOTIFY = "notify:unread:";

}
