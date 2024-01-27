package top.wang3.hami.common.vo.article;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热门文章
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotArticle {

    /**
     * 文章ID
     */
    private Integer articleId;

    /**
     * 热度值
     */
    private Double hotRank;

    /**
     * 文章信息
     */
    private ArticleVo article;
}
