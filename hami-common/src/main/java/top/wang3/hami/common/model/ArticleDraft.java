package top.wang3.hami.common.model;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 文章草稿基本信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "article_draft", autoResultMap = true) //fix: 没有设置autoResultMap导致查询时没有反序列化tagIds
@Valid
public class ArticleDraft {

    /**
     * 草稿ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文章ID, 没有发表时为空
     */
    @TableField(value = "article_id")
    private Integer articleId;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Integer userId;

    /**
     * 标题
     */
    @TableField(value = "title")
    @Size(min = 1, max = 128)
    private String title;

    /**
     * 文章图片地址
     */
    @TableField(value = "picture")
    private String picture;

    /**
     * 文章简介
     */
    @TableField(value = "summary")
    @Size(min = 50, max = 200)
    private String summary;

    /**
     * 文章内容
     */
    @TableField(value = "content")
    @NotEmpty
    private String content;

    /**
     * 文章标签
     */
    @TableField(value = "tag_ids", typeHandler = JacksonTypeHandler.class)
    @Size(min = 1, max = 3, message = "请选择至少一个标签")
    private List<Integer> tagIds;

    /**
     * 分类ID
     */
    @TableField(value = "category_id")
    @NotNull(message = "请选择一个分类")
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
    @JsonIgnore
    private Long version;

    /**
     * 是否删除 0-未删除 1-删除
     */
    @TableField(value = "deleted")
    @TableLogic
    @JsonIgnore
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

    public ArticleDraft(Long id, Integer articleId, Byte state, Long version) {
        this.id = id;
        this.articleId = articleId;
        this.state = state;
        this.version = version;
    }
}