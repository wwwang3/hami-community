package top.wang3.hami.web.controller.article;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wang3.hami.common.dto.ArticleContentDTO;
import top.wang3.hami.common.dto.ArticleDTO;
import top.wang3.hami.common.dto.HotArticleDTO;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.request.ArticlePageParam;
import top.wang3.hami.common.dto.request.UserArticleParam;
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

    @GetMapping("/detail")
    public Result<ArticleContentDTO> getArticleContentById(@RequestParam("articleId") int articleId) {
        ArticleContentDTO articleContentDTO = articleService.getArticleContentById(articleId);
        return Result.ofNullable(articleContentDTO)
                .orElse("文章不存在");
    }

    @GetMapping("/rank/hot")
    public Result<List<HotArticleDTO>> getHotArticles(@RequestParam(value = "category_id", required = false)
                                                          Integer categoryId) {
        List<HotArticleDTO> articles = rankService.getHotArticles(categoryId);
        return Result.successData(articles);
    }

    @GetMapping("/query_list")
    public Result<PageData<ArticleDTO>> getUserArticles(@Valid UserArticleParam param) {
        PageData<ArticleDTO> data = articleService.getUserArticles(param);
        return Result.successData(data);
    }
}
