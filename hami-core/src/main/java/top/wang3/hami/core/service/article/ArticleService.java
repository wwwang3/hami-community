package top.wang3.hami.core.service.article;

import org.springframework.lang.NonNull;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.dto.article.ArticleContentDTO;
import top.wang3.hami.common.dto.article.ArticleDTO;
import top.wang3.hami.common.dto.article.ArticlePageParam;
import top.wang3.hami.common.dto.article.UserArticleParam;
import top.wang3.hami.common.dto.builder.ArticleOptionsBuilder;
import top.wang3.hami.common.model.Article;

import java.util.Collection;
import java.util.List;

public interface ArticleService {

    PageData<ArticleDTO> listNewestArticles(ArticlePageParam param);

    PageData<ArticleDTO> listUserArticle(UserArticleParam param);

    PageData<ArticleDTO> listFollowUserArticles(PageParam param);

    @NonNull
    Long getArticleCount(Integer cateId);

    @NonNull
    Long getUserArticleCount(Integer userId);

    List<Integer> loadArticleListCache(String key, Integer cateId, long current, long size);

    List<Integer> loadUserArticleListCache(String key, Integer userId, long current, long size);

    ArticleContentDTO getArticleContentById(int articleId);

    ArticleDTO getArticleDTOById(Integer id);

    List<ArticleDTO> listArticleById(Collection<Integer> ids, ArticleOptionsBuilder builder);

    boolean saveArticle(Article article);

    boolean updateArticle(Article article);

    boolean deleteByArticleId(Integer userId, Integer articleId);


}
