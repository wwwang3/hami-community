package top.wang3.hami.core.repository;


import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.model.Article;

import java.util.List;

public interface ArticleRepository extends IService<Article> {

    Article getArticleById(Integer articleId);
    List<Article> listArticlesByCateId(Integer cateId);

    String getArticleContentById(Integer articleId);

    List<Article> queryUserArticles(int userId);

    boolean checkArticleExist(Integer articleId);

    boolean saveArticle(Article article);

    boolean updateArticle(Article article);

    boolean deleteArticle(Integer articleId, Integer userId);


}
