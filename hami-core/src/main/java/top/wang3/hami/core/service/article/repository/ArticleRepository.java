package top.wang3.hami.core.service.article.repository;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.model.Article;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ArticleRepository extends IService<Article> {

    Article getArticleInfoById(Integer articleId);

    List<Article> listArticleById(Collection<Integer> ids);

    List<Article> listUserArticle(int userId);

    List<Integer> listFollowUserArticles(Page<Article> page, int loginUserId);

    List<Article> loadArticleListByCateId(Integer cateId);

    List<Integer> loadArticleListByPage(Page<Article> page, Integer cateId, Integer userId);

    String getArticleContentById(Integer articleId);

    List<Integer> searchArticle(Page<Article> page, String keyword);

    List<Integer> searchArticle(Page<Article> page, String keyword, LocalDateTime dateTime);

    List<Integer> searchArticle(Page<Article> page, String keyword, LocalDate localDate);

    List<Integer> searchArticleByFulltextIndex(Page<Article> page, String keyword);

    Integer getArticleAuthor(Integer articleId);

    Map<String, Integer> getArticleCount();

    boolean saveArticle(Article article);

    boolean updateArticle(Article article);

    boolean deleteArticle(Integer articleId, Integer userId);

}
