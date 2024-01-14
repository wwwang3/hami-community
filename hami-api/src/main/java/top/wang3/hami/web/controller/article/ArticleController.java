package top.wang3.hami.web.controller.article;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.dto.article.ArticlePageParam;
import top.wang3.hami.common.dto.article.UserArticleParam;
import top.wang3.hami.common.vo.article.ArticleContentVo;
import top.wang3.hami.core.service.article.ArticleRankService;
import top.wang3.hami.core.service.article.ArticleService;
import top.wang3.hami.security.model.Result;

import java.util.List;

@RestController
@RequestMapping("/api/v1/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    private final ArticleRankService rankService;

    @PostMapping("/list/recommend")
    public Result<PageData<ArticleDTO>> listRecommendArticles(@RequestBody
                                                              @Valid ArticlePageParam param) {
        PageData<ArticleDTO> data = articleService.listNewestArticles(param);
        return Result.ofNullable(data)
                .orElse("获取失败");
    }

    @PostMapping("/follow/query_list")
    public Result<PageData<ArticleDTO>> listFollowUserArticles(@RequestBody @Valid PageParam param) {
        PageData<ArticleDTO> articles = articleService.listFollowUserArticles(param);
        return Result.successData(articles);
    }

    @PostMapping("/user/query_list")
    public Result<PageData<ArticleDTO>> listUserArticles(@RequestBody @Valid UserArticleParam param) {
        PageData<ArticleDTO> data = articleService.listUserArticles(param);
        return Result.successData(data);
    }

    @GetMapping("/detail")
    public Result<ArticleContentVo> getArticleContentById(@RequestParam("article_id") int articleId) {
        ArticleContentVo articleContentDTO = articleService.getArticleContentById(articleId);
        return Result.ofNullable(articleContentDTO)
                .orElse("文章不存在");
    }

    @GetMapping("/rank/hot")
    public Result<List<HotArticleDTO>> listHotArticles(@RequestParam(value = "category_id", required = false)
                                                      Integer categoryId) {
        List<HotArticleDTO> articles = rankService.getHotArticles(categoryId);
        return Result.successData(articles);
    }
}
