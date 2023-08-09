package top.wang3.hami.common.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 文章表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "article")
public class Article {
    /**
     * 文章id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 作者id
     */
    @TableField(value = "user_id")
    private Integer userId;

    /**
     * 文章标题
     */
    @TableField(value = "title")
    private String title;

    /**
     * 文章简介
     */
    @TableField(value = "summary")
    private String summary;

    /**
     * 分类id
     */
    @TableField(value = "category_id")
    private Integer categoryId;

    /**
     * 文章封面
     */
    @TableField(value = "picture")
    private String picture;

    /**
     * 状态 0-正常 1-草稿 2-审核
     */
    @TableField(value = "`state`")
    private Integer state;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @TableField(value = "deleted")
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