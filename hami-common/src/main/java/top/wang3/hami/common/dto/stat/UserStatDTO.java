package top.wang3.hami.common.dto.stat;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 用户数据
 */
@Data
public class UserStatDTO {
    
    /**
     * 用户ID
     */
    @JsonProperty("user_id")
    private Integer userId;

    /**
     * 总关注数
     */
    @JsonProperty("total_followings")
    private Integer totalFollowings = 0;

    /**
     * 总文章数
     */
    @JsonProperty("total_articles")
    private Integer totalArticles = 0;

    /**
     * 总阅读量
     */
    @JsonProperty("total_views")
    private Integer totalViews = 0;

    /**
     * 总获赞数
     */
    @JsonProperty("total_likes")
    private Integer totalLikes = 0;

    /**
     * 总收到的评论数
     */
    @JsonProperty("total_comments")
    private Integer totalComments = 0;

    /**
     * 总收藏数
     */
    @JsonProperty("total_collects")
    private Integer totalCollects = 0;

    /**
     * 总粉丝数
     */
    @JsonProperty("total_followers")
    private Integer totalFollowers = 0;

}
