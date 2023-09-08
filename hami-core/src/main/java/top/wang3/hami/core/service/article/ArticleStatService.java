package top.wang3.hami.core.service.article;

import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.dto.ArticleStatDTO;
import top.wang3.hami.common.dto.UserStat;
import top.wang3.hami.common.model.ArticleStat;

import java.util.List;

public interface ArticleStatService extends IService<ArticleStat> {

    ArticleStatDTO getArticleStatByArticleId(int articleId);

    List<ArticleStatDTO> getArticleStatByArticleIds(List<Integer> articleIds);

    UserStat getUserStatistics(int userId);
    List<UserStat> getUserStatistics(List<Integer> userId);

    boolean increaseViews(int articleId, int count);

    boolean increaseCollects(int articleId, int count);

    boolean increaseComments(int articleId, int count);

    boolean increaseLikes(int articleId, int count);

    boolean decreaseCollects(int articleId, int count);

    boolean decreaseLikes(int articleId, int count);

    boolean decreaseComments(int articleId, int count);


    List<ArticleStat> scanArticleStats(int lastArticle, int batchSize);
}