package top.wang3.hami.common.vo.article;


import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("article_id")
    private Integer articleId;

    /**
     * 热度值
     */
    @JsonProperty("hot_rank")
    private Double hotRank;

    /**
     * 文章信息
     */
    @JsonProperty("article")
    private ArticleVo article;
}
