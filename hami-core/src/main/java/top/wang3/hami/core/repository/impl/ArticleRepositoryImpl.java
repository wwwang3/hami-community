package top.wang3.hami.core.repository.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.ArticleSearchDTO;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.core.exception.ServiceException;
import top.wang3.hami.core.mapper.ArticleMapper;
import top.wang3.hami.core.repository.ArticleRepository;

import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ArticleRepositoryImpl extends ServiceImpl<ArticleMapper, Article>
        implements ArticleRepository {

    private final String[] fields = {"id", "user_id", "title", "summary", "picture", "category_id", "ctime", "mtime"};
    private final String[] full_fields = {"id", "user_id", "title", "summary", "content", "picture", "category_id", "ctime", "mtime"};

    @Override
    public Article getArticleById(Integer articleId) {
        //no content
        if (articleId == null || articleId < 0) {
            throw new ServiceException("参数错误");
        }
        return ChainWrappers.queryChain(getBaseMapper())
                .select(fields)
                .eq("id", articleId)
                .one();
    }

    @Override
    public List<Article> listArticlesByCateId(Integer cateId) {
        List<Article> articles = ChainWrappers.queryChain(getBaseMapper())
                .select("id", "user_id", "ctime")
                .eq(cateId != null, "category_id", cateId)
                .orderByDesc("ctime", "id") //根据ctime倒序
                .last("limit 1000") //查询1000条
                .list();
        if (articles == null || articles.isEmpty()) {
            return Collections.emptyList();
        }
        return articles;
    }

    @Cacheable(cacheNames = Constants.REDIS_CACHE_NAME, key = "'article:content'+#articleId",
            cacheManager = Constants.RedisCacheManager)
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
    public List<Article> queryUserArticles(int userId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("article_id", "ctime")
                .eq("user_id", userId)
                .orderByDesc("ctime")
                .list();
    }

    @Override
    public boolean checkArticleExist(Integer articleId) {
        return getBaseMapper().isArticleExist(articleId);
    }

    @Transactional
    @Override
    public boolean saveArticle(Article article) {
        Assert.notNull(article, "article can not be null");
        return super.save(article);
    }

    @CacheEvict(cacheNames = Constants.REDIS_CACHE_NAME, key = "'article:content'+#article.id",
            cacheManager = Constants.RedisCacheManager)
    @Transactional
    @Override
    public boolean updateArticle(Article article) {
        Assert.notNull(article, "article can not be null");
        return super.updateById(article);
    }


    @CacheEvict(cacheNames = Constants.REDIS_CACHE_NAME, key = "'article:content'+#articleId",
            cacheManager = Constants.RedisCacheManager)
    @Transactional
    @Override
    public boolean deleteArticle(Integer articleId, Integer userId) {
        return ChainWrappers.updateChain(getBaseMapper())
                .eq("user_id", userId)
                .eq("id", articleId)
                .remove();
    }

    @Override
    public List<ArticleSearchDTO> searchArticle(Page<Article> page, String keyword) {
        if (!StringUtils.hasText(keyword)) return Collections.emptyList();
        return getBaseMapper().searchArticle(page, keyword);
    }

    @Override
    public List<Integer> listFollowUserArticles(Page<Article> page, int loginUserId) {
        return getBaseMapper().selectFollowUserArticles(page, loginUserId);
    }


}
