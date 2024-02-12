package top.wang3.hami.web.controller.article;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.dto.article.ArticleDraftParam;
import top.wang3.hami.common.model.ArticleDraft;
import top.wang3.hami.core.service.article.ArticleDraftService;
import top.wang3.hami.security.model.Result;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;

/**
 * draft
 */
@RestController
@RequestMapping("/api/v1/draft")
@RequiredArgsConstructor
public class ArticleDraftController {

    private final ArticleDraftService articleDraftService;

    /**
     * 获取未发布文章草稿
     *
     * @param param {@link PageParam}
     * @return {@link PageData<ArticleDraft>}
     */
    @PostMapping("/list")
    public Result<PageData<ArticleDraft>> listDraft(@RequestBody @Valid PageParam param) {
        PageData<ArticleDraft> articleDrafts = articleDraftService.listDraftByPage(param, Constants.ZERO);
        return Result.successData(articleDrafts);
    }

    /**
     * 获取发布文章草稿
     *
     * @param param {@link PageParam}
     * @return {@link PageData<ArticleDraft>}
     */
    @PostMapping("/article/list")
    public Result<PageData<ArticleDraft>> listArticle(@RequestBody @Valid PageParam param) {
        PageData<ArticleDraft> drafts = articleDraftService.listDraftByPage(param, Constants.ONE);
        return Result.successData(drafts);
    }

    /**
     * 根据Id获取草稿
     *
     * @param draftId 草稿Id
     * @return {@link ArticleDraft}
     */
    @GetMapping("/get/{id}")
    public Result<ArticleDraft> getDraft(@PathVariable("id") Long draftId) {
        ArticleDraft draft = articleDraftService.getArticleDraftById(draftId);
        return Result.ofNullable(draft)
                .orElse("草稿不存在");
    }

    /**
     * 创建草稿
     *
     * @param param {@link ArticleDraftParam}
     * @return {@link ArticleDraft}
     */
    @PostMapping("/create")
    @RateLimit(capacity = 100, interval = 86400L, scope = RateLimit.Scope.LOGIN_USER,
            algorithm = RateLimit.Algorithm.FIXED_WINDOW)
    public Result<ArticleDraft> createDraft(@RequestBody ArticleDraftParam param) {
        ArticleDraft draft = articleDraftService.createDraft(param);
        return Result.successIfNonNull(draft);
    }

    /**
     * 更新文章草稿
     *
     * @param param {@link ArticleDraftParam}
     * @return {@link ArticleDraft}
     */
    @PostMapping("/update")
    public Result<ArticleDraft> updateDraft(@RequestBody ArticleDraftParam param) {
        ArticleDraft draft = articleDraftService.updateDraft(param);
        return Result.successIfNonNull(draft);
    }

    /**
     * 发表文章
     *
     * @param draftId 草稿ID
     * @return {@link ArticleDraft}
     * @description 返回数据包含文章Id
     */
    @PostMapping("/publish")
    public Result<ArticleDraft> publishArticle(@RequestParam("draftId") Long draftId) {
        ArticleDraft draft = articleDraftService.publishArticle(draftId);
        return Result.successIfNonNull(draft);
    }

    /**
     * 删除草稿
     *
     * @param draftId 草稿Id
     * @return 空
     */
    @PostMapping("/delete")
    public Result<Void> deleteDraft(@RequestParam("draftId") long draftId) {
        boolean success = articleDraftService.deleteDraft(draftId);
        return Result.successIfTrue(success);
    }

    /**
     * 删除文章
     *
     * @param articleId 文章Id
     * @return 空
     */
    @PostMapping("/article/delete")
    public Result<Void> deleteArticle(@RequestParam("articleId") int articleId) {
        boolean success = articleDraftService.deleteArticle(articleId);
        return Result.successIfTrue(success);
    }

}
