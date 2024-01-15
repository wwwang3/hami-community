package top.wang3.hami.web.controller.article;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.dto.article.ArticleDraftParam;
import top.wang3.hami.common.model.ArticleDraft;
import top.wang3.hami.common.vo.article.ArticleDraftVo;
import top.wang3.hami.core.service.article.ArticleDraftService;
import top.wang3.hami.security.model.Result;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;

@RestController
@RequestMapping("/api/v1/article_draft")
@RequiredArgsConstructor
public class ArticleDraftController {

    private final ArticleDraftService articleDraftService;

    @GetMapping("/drafts")
    public Result<PageData<ArticleDraftVo>> getDrafts(@RequestParam("pageNum") long pageNum,
                                                      @RequestParam("pageSize") long pageSize) {
        PageParam param = new PageParam(pageNum, pageSize);
        PageData<ArticleDraftVo> articleDrafts = articleDraftService.listDraftByPage(param, Constants.ZERO);
        return Result.successData(articleDrafts);
    }

    @GetMapping("/articles")
    public Result<PageData<ArticleDraftVo>> getArticles(@RequestParam("pageNum") long pageNum,
                                                        @RequestParam("pageSize") long pageSize) {
        PageParam param = new PageParam(pageNum, pageSize);
        PageData<ArticleDraftVo> drafts = articleDraftService.listDraftByPage(param, Constants.ONE);
        return Result.successData(drafts);
    }

    @GetMapping("/get")
    public Result<ArticleDraftVo> getDraft(@RequestParam("draftId") long draftId) {
        ArticleDraftVo draft = articleDraftService.getArticleDraftById(draftId);
        return Result.successData(draft);
    }

    @PostMapping("/create")
    @RateLimit(capacity = 100, interval = 86400L, scope = RateLimit.Scope.LOGIN_USER,
            algorithm = RateLimit.Algorithm.FIXED_WINDOW)
    public Result<ArticleDraft> createDraft(@RequestBody ArticleDraftParam param) {
        ArticleDraft draft = articleDraftService.createDraft(param);
        return Result.successIfNonNull(draft);
    }

    //更新草稿(only draft)
    @PostMapping("/update")
    public Result<ArticleDraft> updateDraft(@RequestBody ArticleDraftParam param) {
        ArticleDraft draft = articleDraftService.updateDraft(param);
        return Result.successIfNonNull(draft);
    }

    //发表文章 只传入draftId即可
    @PostMapping("/publish")
    public Result<ArticleDraft> publishArticle(@RequestParam("draftId") Long draftId) {
        ArticleDraft draft = articleDraftService.publishArticle(draftId);
        return Result.successIfNonNull(draft);
    }

    @PostMapping("/delete/draft")
    public Result<Void> deleteDraft(@RequestParam("draftId") long draftId) {
        boolean success = articleDraftService.deleteDraft(draftId);
        return Result.successIfTrue(success);
    }

    @PostMapping("/delete/article")
    public Result<Void> deleteArticle(@RequestParam("articleId") int articleId) {
        boolean success = articleDraftService.deleteArticle(articleId);
        return Result.successIfTrue(success);
    }

}
