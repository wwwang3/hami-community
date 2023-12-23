package top.wang3.hami.core.service.stat;

import top.wang3.hami.common.dto.stat.ArticleStatDTO;

import java.util.List;
import java.util.Map;

@SuppressWarnings(value = "all")
public interface ArticleStatService {

    ArticleStatDTO getArticleStatByArticleId(int articleId);

    Map<Integer, ArticleStatDTO> listArticleStat(List<Integer> articleIds);

    boolean increaseComments(int articleId, int count);

    boolean decreaseComments(int articleId, int count);

}
