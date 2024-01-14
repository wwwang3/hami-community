package top.wang3.hami.core.service.article;

import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.dto.article.ArticlePageParam;
import top.wang3.hami.common.dto.article.UserArticleParam;
import top.wang3.hami.common.dto.builder.ArticleOptionsBuilder;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.vo.article.ArticleContentVo;
import top.wang3.hami.common.vo.article.ArticleDTO;

import java.util.List;

public interface ArticleService {

    PageData<ArticleDTO> listNewestArticles(ArticlePageParam param);

    PageData<ArticleDTO> listUserArticles(UserArticleParam param);

    PageData<ArticleDTO> listFollowUserArticles(PageParam param);

    /**
     * 根据文章ID批量获取文章(包含用户信息, 不包含用户数据)
     * @param ids 文章ID
     * @param builder builder
     * @return 文章列表
     */
    List<ArticleDTO> listArticleDTOById(List<Integer> ids, ArticleOptionsBuilder builder);

    /**
     * 获取文章信息,包含用户数据和文章内容
     * @param articleId 文章Id
     * @return 文章信息
     */
    ArticleContentVo getArticleContentById(int articleId);


    boolean saveArticle(Article article);

    boolean updateArticle(Article article);

    boolean deleteByArticleId(Integer userId, Integer articleId);

}
