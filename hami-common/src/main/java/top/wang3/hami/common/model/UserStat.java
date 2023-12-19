package top.wang3.hami.common.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName(value = "user_stat")
public class UserStat {
    /**
     * 用户ID
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer userId;

    /**
     * 总关注数
     */
    @TableField(value = "total_followings")
    private Integer totalFollowings;

    /**
     * 总文章数
     */
    @TableField(value = "total_articles")
    private Integer totalArticles;

    /**
     * 总阅读量
     */
    @TableField(value = "total_views")
    private Integer totalViews;

    /**
     * 总获赞数
     */
    @TableField(value = "total_likes")
    private Integer totalLikes;

    /**
     * 总收到的评论数
     */
    @TableField(value = "total_comments")
    private Integer totalComments;

    /**
     * 总收藏数
     */
    @TableField(value = "total_collects")
    private Integer totalCollects;

    /**
     * 总粉丝数
     */
    @TableField(value = "total_followers")
    private Integer totalFollowers;

    /**
     * hot_index
     */
    @TableField(value = "hot_index", exist = false)
    private BigDecimal hotIndex;
}