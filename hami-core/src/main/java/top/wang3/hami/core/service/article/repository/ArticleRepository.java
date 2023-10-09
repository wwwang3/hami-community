package top.wang3.hami.core.service.article.repository;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.dto.article.ArticleInfo;
import top.wang3.hami.common.dto.article.ArticleSearchDTO;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.model.ArticleDO;

import java.util.Collection;
import java.util.List;

public interface ArticleRepository extends IService<Article> {

    ArticleInfo getArticleInfoById(Integer articleId);

    Long getArticleCount(Integer cateId, Integer userId);

    List<ArticleInfo> listArticleById(Collection<Integer> ids);

    List<Article> listUserArticle(int userId);

    List<Integer> listFollowUserArticles(Page<Article> page, int loginUserId);

    List<Article> listArticleByCateId(Integer cateId);

    List<Integer> listArticleByPage(Page<Article> page, Integer cateId, Integer userId);

    String getArticleContentById(Integer articleId);

    List<Integer> scanArticleIds(int lastId, int batchSize);

    List<ArticleDO> scanArticles(List<Integer> ids);

    List<ArticleSearchDTO> searchArticle(Page<Article> page, String keyword);

    Integer getArticleAuthor(Integer articleId);

    boolean saveArticle(Article article);

    boolean updateArticle(Article article);

    boolean deleteArticle(Integer articleId, Integer userId);

}