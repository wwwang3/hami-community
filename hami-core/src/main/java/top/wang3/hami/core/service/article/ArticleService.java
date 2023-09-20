package top.wang3.hami.core.service.article;

import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.article.ArticleContentDTO;
import top.wang3.hami.common.dto.article.ArticleDTO;
import top.wang3.hami.common.dto.article.ArticleInfo;
import top.wang3.hami.common.dto.builder.ArticleOptionsBuilder;
import top.wang3.hami.common.dto.request.ArticlePageParam;
import top.wang3.hami.common.dto.request.PageParam;
import top.wang3.hami.common.dto.request.UserArticleParam;
import top.wang3.hami.common.model.Article;

import java.util.List;

public interface ArticleService {

    ArticleInfo getArticleInfoById(Integer id);

    List<ArticleInfo> getArticleInfoByIds(List<Integer> ids);

    PageData<ArticleDTO> listNewestArticles(ArticlePageParam param);

    ArticleContentDTO getArticleContentById(int articleId);

    List<ArticleDTO> getArticleByIds(List<Integer> ids, ArticleOptionsBuilder builder);

    PageData<ArticleDTO> getUserArticles(UserArticleParam param);

    PageData<ArticleDTO> getFollowUserArticles(PageParam param);

    boolean checkArticleViewLimit(int articleId, int authorId);

    boolean deleteByArticleId(Integer userId, Integer articleId);

    boolean saveArticle(Article article);

    boolean updateArticle(Article article);

}
