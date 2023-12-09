package top.wang3.hami.core.service.article.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;
import top.wang3.hami.common.dto.article.ArticleStatDTO;
import top.wang3.hami.common.dto.user.UserStat;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.common.model.HotCounter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public interface ArticleStatRepository extends IService<ArticleStat> {

    List<ArticleStatDTO> scanArticleStats(int lastArticle, int batchSize);

    List<HotCounter> getHotArticlesByCateId(Integer categoryId, long date);

    List<HotCounter> getOverallHotArticles();

    ArticleStat getArticleStatById(Integer articleId);

    Map<Integer, ArticleStatDTO> getArticleStatByIds(List<Integer> articleIds);

    /**
     * 获取用户数据，不包含关注数据
     * @param userId 用户ID
     * @return UserStat
     */
    UserStat getUserStatByUserId(int userId);

    Map<Integer, UserStat> getUserStatByUserIds(List<Integer> userIds);

    @Transactional(rollbackFor = Exception.class)
    boolean increaseViews(int articleId, int count);

    boolean updateLikes(int articleId, int delta);

    boolean updateComments(int articleId, int delta);

    boolean updateCollects(int articleId, int delta);


    @Transactional(rollbackFor = Exception.class)
    boolean increaseCollects(int articleId, int count);

    @Transactional(rollbackFor = Exception.class)
    boolean increaseComments(int articleId, int count);

    @Transactional(rollbackFor = Exception.class)
    boolean increaseLikes(int articleId, int count);

    @Transactional(rollbackFor = Exception.class)
    boolean decreaseCollects(int articleId, int count);

    @Transactional(rollbackFor = Exception.class)
    boolean decreaseLikes(int articleId, int count);

    @Transactional(rollbackFor = Exception.class)
    boolean decreaseComments(int articleId, int count);

    @Transactional(rollbackFor = Exception.class)
    void updateViews(Collection<ArticleStat> stats);

    @Transactional(rollbackFor = Exception.class)
    boolean deleteArticleStat(Integer articleId);

    Long batchUpdateLikes(List<ArticleStat> articleStats);
}
