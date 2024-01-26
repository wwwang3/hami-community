package top.wang3.hami.common.model;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 文章基本信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "article", autoResultMap = true)
public class Article {

    /**
     * 文章id
     */
    @TableId(value = "id", type = IdType.AUTO)
    @JsonProperty("id")
    private Integer id;

    /**
     * 作者id
     */
    @TableField(value = "user_id")
    @JsonProperty("user_id")
    private Integer userId;

    /**
     * 分类id
     */
    @TableField(value = "category_id")
    @JsonProperty(value = "category_id")
    private Integer categoryId;

    /**
     * 文章标签
     */
    @TableField(value = "tag_ids", typeHandler = JacksonTypeHandler.class)
    @JsonProperty(value = "tag_ids")
    private List<Integer> tagIds;

    /**
     * 文章标题
     */
    @TableField(value = "title")
    @JsonProperty(value = "title")
    private String title;

    /**
     * 文章简介
     */
    @TableField(value = "summary")
    @JsonProperty("summary")
    private String summary;

    /**
     * 文章内容
     */
    @TableField(value = "content")
    @JsonProperty("content")
    private String content;

    /**
     * 文章封面
     */
    @TableField(value = "picture")
    @JsonProperty("picture")
    private String picture;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @TableField(value = "deleted")
    @TableLogic
    @JsonProperty("deleted")
    private Byte deleted;

    /**
     * 创建时间
     */
    @TableField(value = "ctime")
    @JsonProperty("ctime")
    private Date ctime;

    /**
     * 更新时间
     */
    @TableField(value = "mtime")
    @JsonProperty("mtime")
    private Date mtime;

}