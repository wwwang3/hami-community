package top.wang3.hami.core.service.stat.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.common.model.HotCounter;
import top.wang3.hami.common.util.DateUtils;
import top.wang3.hami.core.mapper.ArticleStatMapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
@Slf4j
public class ArticleStatRepositoryImpl extends ServiceImpl<ArticleStatMapper, ArticleStat>
        implements ArticleStatRepository {

    @Override
    public List<ArticleStat> scanArticle(Page<ArticleStat> page) {
        return ChainWrappers.queryChain(getBaseMapper())
                .orderByDesc("article_id")
                .list(page);
    }

    @Override
    public List<HotCounter> loadHotArticle(Integer cateId, LocalDateTime dateTime) {
        if (cateId == null) {
            return getBaseMapper().selectOverallHotArticle(DateUtils.formatDateTime(dateTime));
        } else {
            return getBaseMapper().selectCateHotArticle(cateId, DateUtils.formatDateTime(dateTime));
        }
    }

    @Override
    public ArticleStat selectArticleStatById(Integer articleId) {
        return super.getById(articleId);
    }

    @Override
    public List<ArticleStat> selectArticleStatList(List<Integer> articleIds) {
        return super.listByIds(articleIds);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean increaseViews(int articleId, int count) {
        return ChainWrappers.updateChain(getBaseMapper())
                .setSql("views = views + {0}", count)
                .eq("article_id", articleId)
                .update();
    }

    @Override
    public boolean updateLikes(int articleId, int delta) {
        return ChainWrappers.updateChain(getBaseMapper())
                .setSql("likes = likes + ({0})", delta)
                .eq("article_id", articleId)
                .update();
    }

    @Override
    public boolean updateComments(int articleId, int delta) {
        return ChainWrappers.updateChain(getBaseMapper())
                .setSql("comments = comments + ({0})", delta)
                .eq("article_id", articleId)
                .update();
    }

    @Override
    public boolean updateCollects(int articleId, int delta) {
        return ChainWrappers.updateChain(getBaseMapper())
                .setSql("collects = collects + {0}", delta)
                .eq("article_id", articleId)
                .update();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean increaseCollects(int articleId, int count) {
        return ChainWrappers.updateChain(getBaseMapper())
                .setSql("collects = collects + {0}", count)
                .eq("article_id", articleId)
                .update();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean increaseComments(int articleId, int count) {
        return ChainWrappers.updateChain(getBaseMapper())
                .setSql("comments = comments + {0}", count)
                .eq("article_id", articleId)
                .update();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean increaseLikes(int articleId, int count) {
        return ChainWrappers.updateChain(getBaseMapper())
                .setSql("likes = likes + {0}", count)
                .eq("article_id", articleId)
                .update();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean decreaseCollects(int articleId, int count) {
        return ChainWrappers.updateChain(getBaseMapper())
                .setSql("collects = collects - {0}", count)
                .eq("article_id", articleId)
                .update();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean decreaseLikes(int articleId, int count) {
        return ChainWrappers.updateChain(getBaseMapper())
                .setSql("likes = likes - {0}", count)
                .eq("article_id", articleId)
                .update();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean decreaseComments(int articleId, int count) {
        return ChainWrappers.updateChain(getBaseMapper())
                .setSql("comments = comments - {0}", count)
                .eq("article_id", articleId)
                .update();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateViews(Collection<ArticleStat> stats) {
        super.updateBatchById(stats);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteArticleStat(Integer articleId) {
        return super.removeById(articleId);
    }

    @Override
    public Long batchUpdateLikes(List<ArticleStat> articleStats) {
        return getBaseMapper().batchUpdateLikes(articleStats);
    }

    @Override
    public Long batchUpdateComments(List<ArticleStat> articleStats) {
        return getBaseMapper().batchUpdateComments(articleStats);
    }

    @Override
    public Long batchUpdateCollects(List<ArticleStat> articleStats) {
        return getBaseMapper().batchUpdateCollects(articleStats);
    }

    @Override
    public Long batchUpdateViews(List<ArticleStat> articleStats) {
        return getBaseMapper().batchUpdateViews(articleStats);
    }
}
