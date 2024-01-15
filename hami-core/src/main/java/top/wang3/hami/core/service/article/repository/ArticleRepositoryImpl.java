package top.wang3.hami.core.service.article.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.model.ArticleCount;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.ZPageHandler;
import top.wang3.hami.core.mapper.ArticleMapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ArticleRepositoryImpl extends ServiceImpl<ArticleMapper, Article>
        implements ArticleRepository {

    private final String[] fields = {"id", "user_id", "title", "summary", "picture", "category_id", "ctime", "mtime"};
    private final String[] full_fields = {"id", "user_id", "title", "summary", "content", "picture", "category_id", "ctime", "mtime"};


    @Override
    public Article getArticleInfoById(Integer articleId) {
        return getBaseMapper().selectArticleById(articleId);
    }

    @Override
    public Long getArticleCount(Integer cateId, Integer userId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("id")
                .eq(userId != null, "user_id", userId)
                .eq(cateId != null, "category_id", cateId)
                .count();
    }

    @Override
    public List<Article> listArticleById(Collection<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) return Collections.emptyList();
        return super.listByIds(ids);
    }

    @Override
    public List<Article> listUserArticle(int userId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("id", "ctime")
                .eq("user_id", userId)
                .orderByDesc("ctime")
                .last("limit " + ZPageHandler.DEFAULT_MAX_SIZE)
                .list();
    }

    @Override
    public List<Article> listArticleByCateId(Integer cateId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("id", "user_id", "ctime")
                .eq(cateId != null, "category_id", cateId)
                .orderByDesc("ctime") //根据ctime倒序
                .last("limit " + ZPageHandler.DEFAULT_MAX_SIZE)
                .list();
    }

    @Override
    public List<Integer> listArticleByPage(Page<Article> page, Integer cateId, Integer userId) {
        List<Article> articles = ChainWrappers.queryChain(getBaseMapper())
                .select("id")
                .eq(userId != null, "user_id", userId)
                .eq("category_id", cateId)
                .orderByDesc("ctime")
                .list(page);
        return ListMapperHandler.listTo(articles, Article::getId);
    }

    @Override
    public String getArticleContentById(Integer articleId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("content")
                .eq("id", articleId)
                .oneOpt()
                .map(Article::getContent)
                .orElse("");
    }

    @Override
    public List<Integer> scanArticleIds(int lastId, int batchSize) {
        return getBaseMapper().scanArticleIds(lastId, batchSize);
    }

    @Override
    public List<Article> scanArticleContent(int lastId, int batchSize) {
        return getBaseMapper().scanArticleContent(lastId, batchSize);
    }

    @Override
    public List<Integer> searchArticle(Page<Article> page, String keyword) {
        if (!StringUtils.hasText(keyword)) return Collections.emptyList();
        return getBaseMapper().searchArticle(page, keyword);
    }

    @Override
    public List<Integer> listFollowUserArticles(Page<Article> page, int loginUserId) {
        return getBaseMapper().selectFollowUserArticles(page, loginUserId);
    }

    @Override
    public Integer getArticleAuthor(Integer articleId) {
        return getBaseMapper().selectArticleAuthor(articleId);
    }

    @Override
    public Map<String, Long> getArticleCount() {
        List<ArticleCount> cateArticleCount = getBaseMapper().selectCateArticleCount();
        Long totalArticleCount = getBaseMapper().selectTotalArticleCount();
        Map<String, Long> map = ListMapperHandler.listToMap(
                cateArticleCount,
                item -> RedisConstants.CATE_ARTICLE_COUNT + item.getCateId(),
                ArticleCount::getTotal
        );
        map.put(RedisConstants.TOTAL_ARTICLE_COUNT, totalArticleCount);
        return map;
    }

    @Override
    public boolean saveArticle(Article article) {
        Assert.notNull(article, "article can not be null");
        return super.save(article);
    }

    @Override
    public boolean updateArticle(Article article) {
        Assert.notNull(article, "article can not be null");
        return super.updateById(article);
    }

    @Override
    public boolean deleteArticle(Integer articleId, Integer userId) {
        return ChainWrappers.updateChain(getBaseMapper())
                .eq("user_id", userId)
                .eq("id", articleId)
                .remove();
    }
}
