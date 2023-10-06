package top.wang3.hami.common.dto.article;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleStatDTO {

    /**
     * 文章ID
     */
    private Integer articleId;

    /**
     * 阅读量
     */
    private Integer views = 0;

    /**
     * 点赞数
     */
    private Integer likes = 0;

    /**
     * 评论数
     */
    private Integer comments = 0;

    private Integer collects = 0;

    public ArticleStatDTO(Integer articleId) {
        this.articleId = articleId;
    }
}
