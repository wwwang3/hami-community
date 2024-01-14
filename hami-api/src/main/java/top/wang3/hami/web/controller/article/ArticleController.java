package top.wang3.hami.web.controller.article;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.dto.article.ArticlePageParam;
import top.wang3.hami.common.dto.article.UserArticleParam;
import top.wang3.hami.common.vo.article.ArticleContentVo;
import top.wang3.hami.common.vo.article.ArticleVo;
import top.wang3.hami.core.service.article.ArticleService;
import top.wang3.hami.security.model.Result;

@RestController
@RequestMapping("/api/v1/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @PostMapping("/list/recommend")
    public Result<PageData<ArticleVo>> listRecommendArticles(@RequestBody
                                                              @Valid ArticlePageParam param) {
        PageData<ArticleVo> data = articleService.listNewestArticles(param);
        return Result.ofNullable(data)
                .orElse("获取失败");
    }

    @PostMapping("/follow/query_list")
    public Result<PageData<ArticleVo>> listFollowUserArticles(@RequestBody @Valid PageParam param) {
        PageData<ArticleVo> articles = articleService.listFollowUserArticles(param);
        return Result.successData(articles);
    }

    @PostMapping("/user/query_list")
    public Result<PageData<ArticleVo>> listUserArticles(@RequestBody @Valid UserArticleParam param) {
        PageData<ArticleVo> data = articleService.listUserArticles(param);
        return Result.successData(data);
    }

    @GetMapping("/detail")
    public Result<ArticleContentVo> getArticleContentById(@RequestParam("article_id") int articleId) {
        ArticleContentVo articleContentDTO = articleService.getArticleContentById(articleId);
        return Result.ofNullable(articleContentDTO)
                .orElse("文章不存在");
    }
}
