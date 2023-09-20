package top.wang3.hami.core.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.dto.user.UserStat;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.common.model.HotCounter;

import java.util.Collection;
import java.util.List;


public interface ArticleStatRepository extends IService<ArticleStat> {

    List<ArticleStat> scanArticleStats(int lastArticle, int batchSize);

    List<HotCounter> getHotArticlesByCateId(Integer categoryId);

    List<HotCounter> getOverallHotArticles();

    ArticleStat getArticleStatById(Integer articleId);

    List<ArticleStat> getArticleStatByIds(List<Integer> articleIds);

    /**
     * 获取用户数据，不包含关注数据
     * @param userId 用户ID
     * @return UserStat
     */
    UserStat getUserStatByUserId(int userId);

    List<UserStat> getUserStatByUserIds(List<Integer> userIds);

    void insertArticleStat(Integer articleId, Integer userId);

    boolean increaseViews(int articleId, int count);

    boolean increaseCollects(int articleId, int count);

    boolean increaseComments(int articleId, int count);

    boolean increaseLikes(int articleId, int count);

    boolean decreaseCollects(int articleId, int count);

    boolean decreaseLikes(int articleId, int count);

    boolean decreaseComments(int articleId, int count);

    void updateViews(Collection<ArticleStat> stats);
}
