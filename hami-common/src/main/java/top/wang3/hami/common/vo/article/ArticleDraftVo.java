package top.wang3.hami.common.vo.article;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import top.wang3.hami.common.model.Tag;

import java.util.Date;
import java.util.List;

/**
 * 文章草稿
 */
@Data
public class ArticleDraftVo {


    /**
     * 草稿ID
     */
    @JsonProperty("id")
    private Long id;

    /**
     * 文章ID, 没有发表时为空
     */
    @JsonProperty("article_id")
    private Integer articleId;

    /**
     * 用户ID
     */
    @JsonProperty("user_id")
    private Integer userId;

    /**
     * 标题
     */
    @JsonProperty("title")
    private String title;

    /**
     * 文章图片地址
     */
    @JsonProperty("picture")
    private String picture;

    /**
     * 简介
     */
    @JsonProperty("简介")
    private String summary;

    /**
     * 文章内容
     */
    @JsonProperty("content")
    private String content;

    /**
     * 文章标签
     */
    @JsonProperty("tags")
    private List<Tag> tags;

    /**
     * 分类ID
     */
    @JsonProperty("category_id")
    private Integer categoryId;

    /**
     * 草稿状态 0-未发表 1-已发表
     * @see top.wang3.hami.common.constant.Constants#ONE
     * @see top.wang3.hami.common.constant.Constants#ZERO
     */
    @JsonProperty("state")
    private Byte state;

    /**
     * 创建时间
     */
    @JsonProperty("ctime")
    private Date ctime;

    /**
     * 更新时间
     */
    @JsonProperty("mtime")
    private Date mtime;
}
