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
    private Integer articles = 0;

    /**
     * 文章总阅读量
     */
    private Integer totalViews = 0;

    /**
     * 我收藏的文章数
     */
    private Integer totalCollects = 0;

    /**
     * 文章获得的总点赞数
     */
    private Integer totalLikes = 0;

    /**
     * 总评论数
     */
    private Integer totalComments = 0;

    /**
     * 粉丝数
     */
    private Integer followers = 0;

    /**
     * 关注总数
     */
    private Integer followings = 0;
}
