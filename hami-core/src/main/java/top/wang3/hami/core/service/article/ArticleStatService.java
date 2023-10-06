package top.wang3.hami.core.service.article;

import org.springframework.lang.NonNull;
import top.wang3.hami.common.dto.article.ArticleStatDTO;
import top.wang3.hami.common.dto.user.UserStat;

import java.util.List;
import java.util.Map;

@SuppressWarnings(value = "all")
public interface ArticleStatService {

    ArticleStatDTO getArticleStatByArticleId(int articleId);

    Map<Integer, ArticleStatDTO> listArticleStat(List<Integer> articleIds);

    @NonNull
    UserStat getUserStatByUserId(Integer userId);

    @NonNull
    Map<Integer, UserStat> listUserStat(List<Integer> userIds);

    boolean increaseViews(int articleId, int count);

    boolean increaseCollects(int articleId, int count);

    boolean increaseComments(int articleId, int count);

    boolean increaseLikes(int articleId, int count);

    boolean decreaseCollects(int articleId, int count);

    boolean decreaseLikes(int articleId, int count);

    boolean decreaseComments(int articleId, int count);

}
