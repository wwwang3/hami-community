package top.wang3.hami.common.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ArticleView {

    private int articleId;
    private int authorId;
}
