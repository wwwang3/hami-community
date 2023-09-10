package top.wang3.hami.common.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 文章数据记录表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "article_stat")
public class ArticleStat {
    /**
     * 文章ID
     */
    @TableId(value = "article_id")
    private Integer articleId;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Integer userId;

    /**
     * 阅读量
     */
    @TableField(value = "views", update = "%s+#{et.views}") //就给个%s+1挺恶心的
    private Integer views;

    /**
     * 点赞数
     */
    @TableField(value = "likes")
    private Integer likes;

    /**
     * 评论数
     */
    @TableField(value = "comments")
    private Integer comments;

    /**
     * 收藏数
     */
    @TableField(value = "collects")
    private Integer collects;

    /**
     * 创建时间
     */
    @TableField(value = "ctime")
    private Date ctime;

    /**
     * 更新时间
     */
    @TableField(value = "mtime")
    private Date mtime;

    public ArticleStat(Integer articleId, Integer userId) {
        this.articleId = articleId;
        this.userId = userId;
    }
}