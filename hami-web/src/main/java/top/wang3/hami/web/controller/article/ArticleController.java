package top.wang3.hami.web.controller.article;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wang3.hami.common.dto.ArticleContentDTO;
import top.wang3.hami.common.dto.ArticleDTO;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.request.ArticlePageParam;
import top.wang3.hami.core.service.article.ArticleService;
import top.wang3.hami.security.model.Result;

@RestController
@RequestMapping("/api/v1/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

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
        return Result.successData(articleContentDTO);
    }
}
