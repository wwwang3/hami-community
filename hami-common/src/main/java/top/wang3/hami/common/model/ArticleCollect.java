package top.wang3.hami.common.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
    * 文章收藏表
    */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "article_collect")
public class ArticleCollect {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 收藏夹ID (备用/先不搞收藏夹)
     */
    @TableField(value = "fid")
    private Integer fid;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Integer userId;

    /**
     * 文章ID
     */
    @TableField(value = "article_id")
    private Integer articleId;

    /**
     * 状态
     */
    @TableField(value = "`state`")
    private Byte state;

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
     * 更新时间
     */
    @TableField(value = "mtime")
    private Date mtime;
}