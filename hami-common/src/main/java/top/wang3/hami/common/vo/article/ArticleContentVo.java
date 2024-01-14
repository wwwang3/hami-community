package top.wang3.hami.common.vo.article;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import top.wang3.hami.common.vo.article.ArticleDTO;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ArticleContentVo extends ArticleDTO {
    private String content;
}
