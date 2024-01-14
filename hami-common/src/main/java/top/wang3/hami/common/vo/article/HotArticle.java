package top.wang3.hami.common.vo.article;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotArticle {

    private Integer articleId;
    private Double hotRank;
    private ArticleVo article;
}
