package top.wang3.hami.core.service.article;

import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.dto.article.ArticlePageParam;
import top.wang3.hami.common.dto.article.UserArticleParam;
import top.wang3.hami.common.dto.builder.ArticleOptionsBuilder;
import top.wang3.hami.common.vo.article.ArticleVo;

import java.util.List;

public interface ArticleService {


    /**
     * 分页获取最新文章
     * @param param 分页参数
     * @return PageData
     */
    PageData<ArticleVo> listNewestArticles(ArticlePageParam param);

    /**
     * 分页获取用户文章
     * @param param 分页番薯
     * @return PageData
     */
    PageData<ArticleVo> listUserArticles(UserArticleParam param);

    /**
     * 分页获取某个用户关注的作者的文章
     * @param param 分页参数
     * @return PageData
     */
    PageData<ArticleVo> listFollowUserArticles(PageParam param);

    /**
     * 根据文章ID批量获取文章(包含用户信息, 不包含用户数据)
     * @param ids 文章ID
     * @param builder builder
     * @return 文章列表
     */
    List<ArticleVo> listArticleVoById(List<Integer> ids, ArticleOptionsBuilder builder);

    /**
     * 获取文章信息,包含用户数据和文章内容
     *
     * @param articleId 文章Id
     * @return 文章信息
     */
    ArticleVo getArticleContentById(int articleId);

}
