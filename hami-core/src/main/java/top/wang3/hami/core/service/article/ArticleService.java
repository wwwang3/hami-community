package top.wang3.hami.core.service.article;

import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.dto.ArticleContentDTO;
import top.wang3.hami.common.dto.ArticleDTO;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.request.ArticlePageParam;
import top.wang3.hami.common.model.Article;

public interface ArticleService extends IService<Article> {

    PageData<ArticleDTO> listNewestArticles(ArticlePageParam param);

    ArticleContentDTO getArticleContentById(int articleId);

    boolean checkArticleViewLimit(int articleId, int authorId);
}
