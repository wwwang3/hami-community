package top.wang3.hami.core.service.stat.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.springframework.transaction.annotation.Transactional;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.common.model.HotCounter;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("unused")
public interface ArticleStatRepository extends IService<ArticleStat> {

    List<ArticleStat> scanArticle(Page<ArticleStat> page);

    List<HotCounter> loadHotArticle(Integer cateId, LocalDateTime dateTime);

    ArticleStat selectArticleStatById(Integer articleId);

    List<ArticleStat> selectArticleStatList(List<Integer> articleIds);

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

    @CanIgnoreReturnValue
    boolean deleteArticleStat(Integer articleId);

    @Transactional(rollbackFor = Exception.class)
    Long batchUpdateLikes(List<ArticleStat> articleStats);

    @Transactional(rollbackFor = Exception.class)
    @CanIgnoreReturnValue
    Long batchUpdateComments(List<ArticleStat> articleStats);

    @Transactional(rollbackFor = Exception.class)
    @CanIgnoreReturnValue
    Long batchUpdateCollects(List<ArticleStat> articleStats);

    @Transactional(rollbackFor = Exception.class)
    @CanIgnoreReturnValue
    Long batchUpdateViews(List<ArticleStat> articleStats);

}
