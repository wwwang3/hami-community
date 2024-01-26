package top.wang3.hami.common.dto.stat;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文章数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleStatDTO {

    /**
     * 文章ID
     */
    @JsonProperty("article_id")
    private Integer articleId;

    /**
     * 阅读量
     */
    @JsonProperty("views")
    private Integer views = 0;

    /**
     * 点赞数
     */
    @JsonProperty("likes")
    private Integer likes = 0;

    /**
     * 评论数
     */
    @JsonProperty("comments")
    private Integer comments = 0;

    /**
     * 收藏数
     */
    @JsonProperty("collects")
    private Integer collects = 0;

    public ArticleStatDTO(Integer articleId) {
        this.articleId = articleId;
    }
}
