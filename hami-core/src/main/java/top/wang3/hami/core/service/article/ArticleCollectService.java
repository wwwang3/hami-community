package top.wang3.hami.core.service.article;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.model.ArticleCollect;

import java.util.List;

public interface ArticleCollectService extends IService<ArticleCollect> {

    Long getUserCollects(Integer userId);

    boolean collectArticle(int userId, int articleId);

    boolean cancelCollectArticle(int userId, int articleId);

    /**
     * 获取最新收藏的1000条
     * @param userId
     * @return
     */
    List<ArticleCollect> getUserCollectArticles(Integer userId);

    /**
     * 用于回源DB查询
     * @param page
     * @param userId
     * @return
     */
    List<ArticleCollect> getUserCollectArticles(Page<ArticleCollect> page, Integer userId);

}
