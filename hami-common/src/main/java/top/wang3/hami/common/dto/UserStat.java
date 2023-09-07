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
    private Long articles = 0L;

    /**
     * 文章总阅读量
     */
    private Long totalViews = 0L;

    /**
     * 我收藏的文章数
     */
    private Long totalCollects = 0L;

    /**
     * 文章获得的总点赞数
     */
    private Long totalLikes = 0L;

    /**
     * 总评论数
     */
    private Long totalComments = 0L;

    /**
     * 粉丝数
     */
    private Long followers = 0L;

    /**
     * 关注总数
     */
    private Long followings = 0L;
}
