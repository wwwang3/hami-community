package top.wang3.hami.common.dto.request;


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
    private String title;

    /**
     * 文章图片地址
     */
    private String picture;

    /**
     * 简介
     */
    private String summary;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 文章标签
     */
    private List<Integer> articleTags;


}
