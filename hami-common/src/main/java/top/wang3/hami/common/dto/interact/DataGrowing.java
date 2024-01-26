package top.wang3.hami.common.dto.interact;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 昨日数据变化
 */
@Data
public class DataGrowing {

    /**
     * 用户ID
     */
    @JsonProperty("user_id")
    private Integer userId;

    /**
     * 日期
     */
    private String date;

    /**
     * 文章数量变化
     */
    @JsonProperty("article_incr")
    private Integer articleIncr;

    /**
     * 文章阅读量变化
     */
    @JsonProperty("view_incr")
    private Integer viewIncr;

    /**
     * 文章点赞数变化
     */
    @JsonProperty("like_incr")
    private Integer LikeIncr;

    /**
     * 文章评论数变化
     */
    @JsonProperty("comment_incr")
    private Integer commentIncr;

    /**
     * 收藏数变化
     */
    @JsonProperty("collect_incr")
    private Integer collectIncr;

    /**
     * 粉丝数变化
     */
    @JsonProperty("follower_incr")
    private Integer followerIncr;

    /**
     * 取关数变化
     */
    @JsonProperty("cancel_follow_incr")
    private Integer cancelFollowIncr;
}
