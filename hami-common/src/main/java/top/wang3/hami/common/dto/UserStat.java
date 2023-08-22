package top.wang3.hami.common.dto;

import lombok.Data;


/**
 * 用户数据
 */
@Data
public class UserStat {

    private int userId;

    /**
     * 文章数
     */
    private int articles;

    /**
     * 文章总阅读量
     */
    private int views;

    /**
     * 我点赞的文章数
     */
    private int likes;

    /**
     * 我收藏的文章数
     */
    private int collects;

    /**
     * 文章被收藏总数
     */
    private int gotCollects;

    /**
     * 文章评论总数
     */
    private int gotComments;

    /**
     * 文章的获得的点赞总数
     */
    private int gotLikes;

    /**
     * 粉丝数
     */
    private int followers;

    /**
     * 关注总数
     */
    private int followings;
}
