package top.wang3.hami.core.service.article.repository;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.dto.article.ArticleInfo;
import top.wang3.hami.common.model.Article;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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

    List<Article> scanArticleContent(int lastId, int batchSize);

    List<Integer> searchArticle(Page<Article> page, String keyword);

    Integer getArticleAuthor(Integer articleId);

    Map<String, Long> getArticleCount();

    boolean saveArticle(Article article);

    boolean updateArticle(Article article);

    boolean deleteArticle(Integer articleId, Integer userId);

}
