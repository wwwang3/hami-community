package top.wang3.hami.common.dto.stat;


import lombok.Data;

@Data
public class UserStatDTO {


    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 总关注数
     */
    private Integer totalFollowings = 0;

    /**
     * 总文章数
     */
    private Integer totalArticles = 0;

    /**
     * 总阅读量
     */
    private Integer totalViews = 0;

    /**
     * 总获赞数
     */
    private Integer totalLikes = 0;

    /**
     * 总收到的评论数
     */
    private Integer totalComments = 0;

    /**
     * 总收藏数
     */
    private Integer totalCollects = 0;

    /**
     * 总粉丝数
     */
    private Integer totalFollowers = 0;

}
