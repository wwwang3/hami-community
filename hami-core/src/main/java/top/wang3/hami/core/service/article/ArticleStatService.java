package top.wang3.hami.core.service.article;

import top.wang3.hami.common.dto.article.ArticleStatDTO;

import java.util.List;

public interface ArticleStatService {

    ArticleStatDTO getArticleStatByArticleId(int articleId);

    List<ArticleStatDTO> getArticleStatByArticleIds(List<Integer> articleIds);

    boolean increaseViews(int articleId, int count);

    boolean increaseCollects(int articleId, int count);

    boolean increaseComments(int articleId, int count);

    boolean increaseLikes(int articleId, int count);

    boolean decreaseCollects(int articleId, int count);

    boolean decreaseLikes(int articleId, int count);

    boolean decreaseComments(int articleId, int count);

}
