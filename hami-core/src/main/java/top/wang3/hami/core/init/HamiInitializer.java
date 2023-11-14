package top.wang3.hami.core.init;

public interface HamiInitializer extends Runnable {

    String getName();

    void run();

    String LOCAL_CACHE = "local_cache";

    /**
     * 文章列表
     */
    String ARTICLE_LIST_CACHE = "article_list_cache";

    /**
     * 收藏，点赞，关注，粉丝列表
     */
    String INTERACT_LIST = "interact_list";

    /**
     * 文章
     */
    String ARTICLE_CACHE = "article_cache";

    /**
     * 用户
     */
    String USER_CACHE = "article_cache";

    /**
     * 数据
     */
    String STAT_CACHE = "stat_cache";

    /**
     * 热门文章
     */
    String HOT_ARTICLE = "hot_article";
}
