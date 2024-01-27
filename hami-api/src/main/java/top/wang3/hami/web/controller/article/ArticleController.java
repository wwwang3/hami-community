package top.wang3.hami.web.controller.article;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.dto.article.ArticlePageParam;
import top.wang3.hami.common.dto.article.UserPageParam;
import top.wang3.hami.common.vo.article.ArticleVo;
import top.wang3.hami.core.service.article.ArticleService;
import top.wang3.hami.security.model.Result;
import top.wang3.hami.web.annotation.Public;

/**
 * article
 */
@RestController
@RequestMapping("/api/v1/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    /**
     * 查询最新文章
     *
     * @return {@link PageData<ArticleVo>}
     */
    @Public
    @PostMapping("/list/recommend")
    public Result<PageData<ArticleVo>> listRecommendArticles(@RequestBody @Valid ArticlePageParam param) {
        PageData<ArticleVo> data = articleService.listNewestArticles(param);
        return Result.ofNullable(data)
                .orElse("获取失败");
    }

    /**
     * 分页查询关注用户的文章
     *
     * @param param {@link PageParam}
     * @return {@link PageData<ArticleVo>}
     */
    @PostMapping("/follow/query_list")
    public Result<PageData<ArticleVo>> listFollowUserArticles(@RequestBody @Valid PageParam param) {
        PageData<ArticleVo> articles = articleService.listFollowUserArticles(param);
        return Result.successData(articles);
    }

    /**
     * 分页查询用户文章
     *
     * @param param {@link UserPageParam}
     * @return {@link PageData<ArticleVo>}
     */
    @Public
    @PostMapping("/user/query_list")
    public Result<PageData<ArticleVo>> listUserArticles(@RequestBody @Valid UserPageParam param) {
        PageData<ArticleVo> data = articleService.listUserArticles(param);
        return Result.successData(data);
    }

    /**
     * 获取文章内容
     *
     * @param articleId 文章Id
     * @return {@link ArticleVo}
     */
    @Public
    @GetMapping("/detail/{id}")
    public Result<ArticleVo> getArticleContentById(@PathVariable("id") int articleId) {
        return Result.ofNullable(articleService.getArticleContentById(articleId))
                .orElse("文章不存在");
    }
}
