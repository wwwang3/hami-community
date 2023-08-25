package top.wang3.hami.common.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
    * 文章草稿表
    */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "article_draft")
public class ArticleDraft {
    /**
     * 主键ID,草稿ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Integer userId;

    /**
     * 标题
     */
    @TableField(value = "title")
    private String title;

    /**
     * 文章图片地址
     */
    @TableField(value = "picture")
    private String picture;

    /**
     * 文章内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 文章标签
     */
    @TableField(value = "article_tags", typeHandler = JacksonTypeHandler.class)
    private String articleTags;

    /**
     * 分类ID
     */
    @TableField(value = "category_id")
    private Integer categoryId;

    /**
     * 草稿状态 0-未发表 1-已发表
     */
    @TableField(value = "`state`")
    private Byte state;

    /**
     * 版本号
     */
    @TableField(value = "version")
    private Long version;

    /**
     * 是否删除 0-未删除 1-删除
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