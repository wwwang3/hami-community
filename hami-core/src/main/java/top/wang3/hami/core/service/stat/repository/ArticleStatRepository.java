package top.wang3.hami.core.service.stat.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;
import top.wang3.hami.common.dto.stat.ArticleStatDTO;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.common.model.HotCounter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public interface ArticleStatRepository extends IService<ArticleStat> {

    List<ArticleStatDTO> scanArticleStats(int lastArticle, int batchSize);

    List<HotCounter> getHotArticlesByCateId(Integer categoryId, long date);

    List<HotCounter> getOverallHotArticles(long timestamp);

    ArticleStat getArticleStatById(Integer articleId);

    Map<Integer, ArticleStatDTO> getArticleStatByIds(List<Integer> articleIds);

    @Transactional(rollbackFor = Exception.class)
    boolean increaseViews(int articleId, int count);

    boolean updateLikes(int articleId, int delta);

    boolean updateComments(int articleId, int delta);

    boolean updateCollects(int articleId, int delta);


    boolean increaseCollects(int articleId, int count);

    boolean increaseComments(int articleId, int count);

    boolean increaseLikes(int articleId, int count);

    boolean decreaseCollects(int articleId, int count);

    boolean decreaseLikes(int articleId, int count);

    boolean decreaseComments(int articleId, int count);

    void updateViews(Collection<ArticleStat> stats);

    boolean deleteArticleStat(Integer articleId);

    @Transactional(rollbackFor = Exception.class)
    Long batchUpdateLikes(List<ArticleStat> articleStats);

    @Transactional(rollbackFor = Exception.class)
    Long batchUpdateComments(List<ArticleStat> articleStats);

    @Transactional(rollbackFor = Exception.class)
    Long batchUpdateCollects(List<ArticleStat> articleStats);

    @Transactional(rollbackFor = Exception.class)
    Long batchUpdateViews(List<ArticleStat> articleStats);

}
