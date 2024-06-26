package top.wang3.hami.common.model;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wang3.hami.common.dto.IpInfo;

import java.util.Date;
import java.util.List;

/**
 * 评论基本信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "`comment`", autoResultMap = true)
public class Comment {
    /**
     * 评论ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 文章ID
     */
    @TableField(value = "article_id")
    private Integer articleId;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Integer userId;

    /**
     * 顶级评论ID 0-表示是根评论
     */
    @TableField(value = "root_id")
    private Integer rootId;

    /**
     * 父评论ID
     */
    @TableField(value = "parent_id")
    private Integer parentId;

    /**
     * 回复的用户ID
     */
    @TableField(value = "reply_to")
    private Integer replyTo;

    /**
     * 评论时的IP信息
     */
    @TableField(value = "ip_info", typeHandler = JacksonTypeHandler.class)
    private IpInfo ipInfo;

    /**
     * 评论内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 评论图片
     */
    @TableField(value = "pictures", typeHandler = JacksonTypeHandler.class)
    private List<String> pictures;

    /**
     * 点赞数
     */
    @TableField(value = "likes")
    private Integer likes = 0;

    /**
     * 是否删除
     */
    @TableField(value = "deleted")
    @TableLogic
    private Byte deleted;

    /**
     * 创建时间
     */
    @TableField(value = "ctime")
    private Date ctime;

    /**
     * 最后更新时间
     */
    @TableField(value = "mtime")
    private Date mtime;
}