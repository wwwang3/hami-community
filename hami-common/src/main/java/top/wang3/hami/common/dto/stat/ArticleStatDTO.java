package top.wang3.hami.common.dto.stat;


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

    /**
     * 收藏数
     */
    private Integer collects = 0;

    public ArticleStatDTO(Integer articleId) {
        this.articleId = articleId;
    }
}
