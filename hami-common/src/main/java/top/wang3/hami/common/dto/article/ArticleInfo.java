package top.wang3.hami.common.dto.article;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleInfo {

    /**
     * 文章id
     */
    private Integer id;

    /**
     * 作者id
     */
    private Integer userId;

    /**
     * 分类id
     */
    private Integer categoryId;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章简介
     */
    private String summary;

    /**
     * 文章封面
     */
    private String picture;

    /**
     * 创建时间
     */
    private Date ctime;

    /**
     * 更新时间
     */
    private Date mtime;

    //todo
    private byte deleted;

    /**
     * 文章标签
     */
    private List<Integer> tagIds;
}
