package top.wang3.hami.core.repository;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.dto.article.ArticleInfo;
import top.wang3.hami.common.dto.article.ArticleSearchDTO;
import top.wang3.hami.common.model.Article;

import java.util.List;

public interface ArticleRepository extends IService<Article> {

    ArticleInfo getArticleInfoById(Integer articleId);

    List<ArticleInfo> listArticleById(List<Integer> ids);

    List<Article> listArticleByCateId(Integer cateId);


    String getArticleContentById(Integer articleId);

    List<Article> queryUserArticles(int userId);

    boolean checkArticleExist(Integer articleId);

    boolean saveArticle(Article article);

    boolean updateArticle(Article article);

    boolean deleteArticle(Integer articleId, Integer userId);

    List<ArticleSearchDTO> searchArticle(Page<Article> page, String keyword);

    List<Integer> listFollowUserArticles(Page<Article> page, int loginUserId);

    List<Integer> listInitArticle();
}
