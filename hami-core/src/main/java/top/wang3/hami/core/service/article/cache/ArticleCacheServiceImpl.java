package top.wang3.hami.core.service.article.cache;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.zset.Tuple;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.constant.TimeoutConstants;
import top.wang3.hami.common.dto.article.ArticleInfo;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.common.util.ZPageHandler;
import top.wang3.hami.common.vo.article.HotArticle;
import top.wang3.hami.core.cache.CacheService;
import top.wang3.hami.core.service.article.repository.ArticleRepository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleCacheServiceImpl implements ArticleCacheService {

    private final ArticleRepository articleRepository;
    private final CacheService cacheService;

    @Override
    public long getArticleCountCache(Integer cateId) {
        String key = RedisConstants.ARTICLE_COUNT_KEY;
        String hKey = (cateId == null) ? RedisConstants.TOTAL_ARTICLE_COUNT :
                RedisConstants.CATE_ARTICLE_COUNT + cateId;
        return cacheService.getMapValue(
                key,
                hKey,
                articleRepository::getArticleCount,
                TimeoutConstants.ARTICLE_COUNT_EXPIRE,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public long getUserArticleCountCache(Integer userId) {
        String key = RedisConstants.USER_ARTICLE_COUNT + userId;
        return cacheService.get(
                key,
                () -> articleRepository.getArticleCount(null, userId),
                TimeoutConstants.USER_ARTICLE_LIST_EXPIRE,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public List<Integer> listArticleIdByPage(Page<Article> page, Integer cateId) {
        // 从缓存中读取文章列表
        String key = getArticleListKey(cateId);
        return ZPageHandler.<Integer>of(key, page)
                .countSupplier(() -> getArticleCountCache(cateId))
                .source((current, size) -> articleRepository.listArticleByPage(page, cateId, null))
                .loader(() -> this.loadArticleListCache(cateId))
                .query();

    }

    @Override
    public List<Integer> listUserArticleIdByPage(Page<Article> page, Integer userId) {
        // 从缓存中获取用户Id
        String key = RedisConstants.USER_ARTICLE_LIST + userId;
        return ZPageHandler.<Integer>of(key, page)
                .countSupplier(() -> getUserArticleCountCache(userId))
                .source((current, size) -> articleRepository.listArticleByPage(page, null, userId))
                .loader(() -> loadUserArticleListCache(userId))
                .query();
    }

    @Override
    public List<ArticleInfo> listArticleInfoById(List<Integer> articleIds) {
        // 从缓存中获取articleInfo
        return cacheService.multiGetById(
                RedisConstants.ARTICLE_INFO,
                articleIds,
                articleRepository::listArticleById,
                TimeoutConstants.ARTICLE_INFO_EXPIRE,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public List<HotArticle> listHotArticle(Integer cateId) {
        String key = cateId == null ? RedisConstants.TOTAL_HOT_ARTICLE : RedisConstants.CATE_HOT_ARTICLE + cateId;
        // 数量较少, 直接读了
        Set<ZSetOperations.TypedTuple<Integer>> typedTuples = RedisClient.zRevRangeWithScore(key, 0, -1);
        return ListMapperHandler.listTo(typedTuples, item -> {
            HotArticle hotArticle = new HotArticle();
            hotArticle.setArticleId(item.getValue());
            hotArticle.setHotRank(item.getScore());
            return hotArticle;
        });
    }

    @Override
    public ArticleInfo getArticleInfoCache(Integer articleId) {
        // 从缓存获取article-info
        return cacheService.get(
                RedisConstants.ARTICLE_INFO + articleId,
                () -> articleRepository.getArticleInfoById(articleId),
                TimeoutConstants.ARTICLE_INFO_EXPIRE,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public String getArticleContentCache(Integer articleId) {
        return cacheService.get(
                RedisConstants.ARTICLE_CONTENT + articleId,
                () -> articleRepository.getArticleContentById(articleId),
                TimeoutConstants.ARTICLE_INFO_EXPIRE,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public List<Integer> loadArticleListCache(Integer cateId) {
        // 从数据库读取
        List<Article> articles = articleRepository.listArticleByCateId(cateId);
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
