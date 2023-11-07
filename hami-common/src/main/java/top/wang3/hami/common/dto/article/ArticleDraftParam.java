package top.wang3.hami.common.dto.article;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

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
    private Integer categoryId;

    /**
     * 文章标签
     */
    @NotEmpty
    @Size(min = 1, max = 3)
    private List<Integer> tagIds;


}
