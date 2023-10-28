package top.wang3.hami.core.service.article.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import top.wang3.hami.common.dto.article.ArticleStatDTO;
import top.wang3.hami.common.dto.user.UserStat;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.common.model.HotCounter;
import top.wang3.hami.core.mapper.ArticleStatMapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class ArticleStatRepositoryImpl extends ServiceImpl<ArticleStatMapper, ArticleStat>
        implements ArticleStatRepository {

    @Override
    public List<ArticleStat> scanArticleStats(int lastArticle, int batchSize) {
        return getBaseMapper().scanBatchStats(lastArticle, batchSize);
    }

    @Override
    public List<HotCounter> getHotArticlesByCateId(Integer categoryId, long date) {
        return getBaseMapper().selectHotArticlesByCateId(categoryId, date);
    }

    @Override
    public List<HotCounter> getOverallHotArticles() {
        return getBaseMapper().selectHotArticles();
    }

    @Override
    public ArticleStat getArticleStatById(Integer articleId) {
        return super.getById(articleId);
    }

    @Override
    public Map<Integer, ArticleStatDTO> getArticleStatByIds(List<Integer> articleIds) {
        return getBaseMapper().selectArticleStatsByArticleIds(articleIds);
    }

    @Override
    public UserStat getUserStatByUserId(int userId) {
        return getBaseMapper().selectUserStat(userId);
    }

    @Override
    public Map<Integer, UserStat> getUserStatByUserIds(List<Integer> userIds) {
        return getBaseMapper().selectUserStatsByUserIds(userIds);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean increaseViews(int articleId, int count) {
        return ChainWrappers.updateChain(getBaseMapper())
                .setSql("views = views + {0}", count)
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
}
