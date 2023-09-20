package top.wang3.hami.common.dto;


import lombok.Data;

import java.util.List;

@Data
public class ArticleDTO {

    private Integer id;

    private Integer userId;

    /**
     * 文章信息
     */
    private ArticleInfo articleInfo;

    /**
     * 作者信息
     */
    private UserDTO author;

    /**
     * 分类
     */
    private CategoryDTO category;

    /**
     * 文章标签
     */
    private List<TagDTO> tags;

    /**
     * 文章数据
     */
    private ArticleStatDTO stat;

    private boolean liked = false;
    private boolean collected = false;

}
