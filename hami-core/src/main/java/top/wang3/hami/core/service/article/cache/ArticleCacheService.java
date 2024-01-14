package top.wang3.hami.core.service.article.cache;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import top.wang3.hami.common.dto.article.ArticleInfo;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.vo.article.HotArticle;

import java.util.List;


/**
 * 文章缓存服务, 用于加载文章数缓存, 文章列表缓存
 */
public interface ArticleCacheService {

    long getArticleCountCache(Integer cateId);

    long getUserArticleCountCache(Integer userId);

    List<Integer> listArticleIdByPage(Page<Article> page, Integer cateId);

    List<Integer> listUserArticleIdByPage(Page<Article> page, Integer userId);

    List<ArticleInfo> listArticleInfoById(List<Integer> articleIds);

    List<HotArticle> listHotArticle(Integer cateId);

    ArticleInfo getArticleInfoCache(Integer articleId);

    String getArticleContentCache(Integer articleId);

    List<Integer> loadArticleListCache(Integer cateId);

    List<Integer> loadUserArticleListCache(Integer userId);

    List<Integer> loadUserArticleListCache(Integer userId);

}
