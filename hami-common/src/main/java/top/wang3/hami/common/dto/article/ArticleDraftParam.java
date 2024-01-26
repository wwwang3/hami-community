package top.wang3.hami.common.dto.article;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 文章草稿请求参数
 */
@Data
public class ArticleDraftParam {

    /**
     * 主键ID,草稿ID
     */
    private Long id;

    /**
     * 标题
     */
    @Size(min = 1, max = 128)
    private String title;

    /**
     * 文章图片地址
     */
    private String picture;

    /**
     * 简介
     */
    @Size(min = 50, max = 200)
    private String summary;

    /**
     * 文章内容
     */
    @NotEmpty
    private String content;

    /**
     * 分类ID
     */
    @NotNull
    @JsonProperty("category_id")
    private Integer categoryId;

    /**
     * 文章标签
     */
    @NotEmpty
    @Size(min = 1, max = 3)
    @JsonProperty("tag_ids")
    private List<Integer> tagIds;


}
