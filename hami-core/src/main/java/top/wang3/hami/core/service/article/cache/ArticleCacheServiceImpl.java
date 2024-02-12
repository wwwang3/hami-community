package top.wang3.hami.core.service.article.cache;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.zset.Tuple;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.constant.TimeoutConstants;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.common.util.ZPageHandler;
import top.wang3.hami.core.cache.CacheService;
import top.wang3.hami.core.service.article.repository.ArticleRepository;
import top.wang3.hami.core.service.stat.CountService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleCacheServiceImpl implements ArticleCacheService {

    private final ArticleRepository articleRepository;
    private final CacheService cacheService;
    private final CountService countService;

    @Override
    public int getArticleCountCache(Integer cateId) {
        String key = RedisConstants.ARTICLE_COUNT_HASH;
        String hKey = (cateId == null) ? RedisConstants.TOTAL_ARTICLE_COUNT_HKEY :
                RedisConstants.CATE_ARTICLE_COUNT_HKEY + cateId;
        return cacheService.getHashValue(
                key,
                hKey,
                articleRepository::getArticleCount,
                TimeoutConstants.ARTICLE_COUNT_EXPIRE
        );
    }

    @Override
    public List<Integer> listArticleIdByPage(Page<Article> page, Integer cateId) {
        // 从缓存中读取文章列表
        String key = getArticleListKey(cateId);
        return ZPageHandler.<Integer>of(key, page)
                .countSupplier(() -> getArticleCountCache(cateId))
                .source((current, size) -> articleRepository.loadArticleListByPage(page, cateId, null))
                .loader(() -> this.loadArticleListCache(cateId))
                .query();

    }

    @Override
    public List<Integer> listUserArticleIdByPage(Page<Article> page, Integer userId) {
        // 从缓存中获取文章ID
        String key = RedisConstants.USER_ARTICLE_LIST + userId;
        return ZPageHandler.<Integer>of(key, page)
                .countSupplier(() -> countService.getUserArticleCount(userId))
                .source((current, size) -> articleRepository.loadArticleListByPage(page, null, userId))
                .loader(() -> loadUserArticleListCache(userId))
                .query();
    }

    @Override
    public List<Article> listArticleInfoById(List<Integer> articleIds) {
        // 从缓存中获取articleInfo
        return cacheService.multiGetById(
                RedisConstants.ARTICLE_INFO,
                articleIds,
                articleRepository::listArticleById,
                Article::getId,
                TimeoutConstants.ARTICLE_INFO_EXPIRE
        );
    }

    @Override
    public Article getArticleInfoCache(Integer articleId) {
        // 从缓存获取article-info
        return cacheService.get(
                RedisConstants.ARTICLE_INFO + articleId,
                () -> articleRepository.getArticleInfoById(articleId),
                TimeoutConstants.ARTICLE_INFO_EXPIRE
        );
    }

    @Override
    public String getArticleContentCache(Integer articleId) {
        return cacheService.get(
                RedisConstants.ARTICLE_CONTENT + articleId,
                () -> articleRepository.getArticleContentById(articleId),
                TimeoutConstants.ARTICLE_INFO_EXPIRE
        );
    }

    @Override
    public List<Integer> loadArticleListCache(Integer cateId) {
        // 从数据库读取
        List<Article> articles = articleRepository.loadArticleListByCateId(cateId);
        // fix: zset不能为空
        if (CollectionUtils.isEmpty(articles)) return Collections.emptyList();
        String articleListKey = getArticleListKey(cateId);
        List<Integer> ids = ListMapperHandler.listTo(articles, Article::getId);
        // 刷新缓存
        Collection<Tuple> tuples = ListMapperHandler.listToTuple(
                articles,
                Article::getId,
                item -> item.getCtime().getTime()
        );
        RedisClient.zSetAll(
                articleListKey,
                tuples,
                TimeoutConstants.ARTICLE_LIST_EXPIRE,
                TimeUnit.MILLISECONDS
        );
        return ids;
    }

    @Override
    public List<Integer> loadUserArticleListCache(Integer userId) {
        String key = RedisConstants.USER_ARTICLE_LIST + userId;
        List<Article> articles = articleRepository.listUserArticle(userId);
        // zset不能为空
        if (CollectionUtils.isEmpty(articles)) return Collections.emptyList();
        List<Integer> ids = ListMapperHandler.listTo(articles, Article::getId);
        Collection<Tuple> tuples = ListMapperHandler.listToTuple(
                articles,
                Article::getId,
                item -> item.getCtime().getTime()
        );
        // 刷新缓存
        RedisClient.zSetAll(
                key,
                tuples,
                TimeoutConstants.USER_ARTICLE_LIST_EXPIRE,
                TimeUnit.MILLISECONDS
        );
        return ids;
    }

    private String getArticleListKey(Integer cateId) {
        return cateId == null ? RedisConstants.ARTICLE_LIST : RedisConstants.CATE_ARTICLE_LIST + cateId;
    }

}
