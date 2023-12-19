package top.wang3.hami.core.service.article;

import org.springframework.lang.NonNull;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.dto.article.*;
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

    /**
     * 获取文章信息,包含用户数据和文章内容
     * @param articleId 文章Id
     * @return 文章信息
     */
    ArticleContentDTO getArticleContentById(int articleId);

    /**
     * 根据文章ID批量获取文章(包含用户信息, 不包含用户数据)
     * @param ids 文章ID
     * @param builder builder
     * @return 文章列表
     */
    List<ArticleDTO> listArticleDTOById(Collection<Integer> ids, ArticleOptionsBuilder builder);

    boolean saveArticle(Article article);

    boolean updateArticle(Article article);

    boolean deleteByArticleId(Integer userId, Integer articleId);

    List<ArticleInfo> loadArticleInfoCache(List<Integer> nullIds);

    Long loadArticleCount(String key, String hKey);
}
