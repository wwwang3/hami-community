package top.wang3.hami.web.controller.article;


import org.springframework.web.bind.annotation.*;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.request.ArticleDraftParam;
import top.wang3.hami.common.dto.request.PageParam;
import top.wang3.hami.common.model.ArticleDraft;
import top.wang3.hami.core.service.article.ArticleDraftService;
import top.wang3.hami.security.model.Result;

@RestController
@RequestMapping("/api/v1/article_draft")
public class ArticleDraftController {


    private final ArticleDraftService articleDraftService;

    public ArticleDraftController(ArticleDraftService articleDraftService) {
        this.articleDraftService = articleDraftService;
    }

    @GetMapping("/drafts")
    public Result<PageData<ArticleDraft>> getArticleDrafts(@RequestParam("pageNum") long pageNum,
                                                           @RequestParam("pageSize") long pageSize) {
        PageParam param = new PageParam(pageNum, pageSize);
        PageData<ArticleDraft> articleDrafts = articleDraftService.getArticleDrafts(param, Constants.ZERO);
        return Result.success(articleDrafts);
    }

    @GetMapping("/get")
    public Result<ArticleDraft> getDraft(@RequestParam("draftId") long draftId) {
        ArticleDraft draft = articleDraftService.getArticleDraftById(draftId);
        return Result.success(draft);
    }

    @PostMapping("/update")
    public Result<ArticleDraft> saveArticleDraft(@RequestBody ArticleDraftParam param) {
        ArticleDraft draft = articleDraftService.saveOrUpdateArticleDraft(param);
        return Result.success(draft);
    }

    @PostMapping("/publish")
    public Result<ArticleDraft> publishArticle(@RequestBody ArticleDraftParam param) {
        ArticleDraft draft = articleDraftService.publishArticle(param);
        return Result.success(draft);
    }
}
