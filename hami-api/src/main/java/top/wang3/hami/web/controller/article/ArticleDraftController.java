package top.wang3.hami.web.controller.article;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wang3.hami.common.dto.ArticleDraftPageParam;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.article.ArticleDraftParam;
import top.wang3.hami.common.model.ArticleDraft;
import top.wang3.hami.core.service.article.ArticleDraftService;
import top.wang3.hami.security.context.LoginUserContext;
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
     * 获取草稿列表
     *
     * @param param {@link ArticleDraftPageParam}
     * @return {@link PageData<ArticleDraft>}
     */
    @PostMapping("/list")
    public Result<PageData<ArticleDraft>> listDraft(@RequestBody @Valid ArticleDraftPageParam param) {
        // 获取当前登录用户的
        int loginUserId = LoginUserContext.getLoginUserId();
        param.setUserId(loginUserId);
        PageData<ArticleDraft> data = articleDraftService.listDraft(param);
        return Result.successData(data);
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
    @RateLimit(capacity = 100, interval = 86400L, scope = RateLimit.Scope.LOGIN_USER,
        algorithm = RateLimit.Algorithm.FIXED_WINDOW)
    public Result<ArticleDraft> updateDraft(@RequestBody ArticleDraftParam param) {
        ArticleDraft draft = articleDraftService.updateDraft(param);
        return Result.successIfNonNull(draft);
    }

    /**
     * 发表文章
     *
     * @param draftId 草稿ID
     * @description 发表文章, 将文章状态更新为审核
     */
    @PostMapping("/publish")
    @RateLimit(capacity = 100, interval = 86400L, scope = RateLimit.Scope.LOGIN_USER,
        algorithm = RateLimit.Algorithm.FIXED_WINDOW)
    public Result<Void> publishOrUpdate(@RequestParam("draftId") Long draftId) {
        articleDraftService.publishOrUpdate(draftId);
        return Result.success();
    }

    /**
     * 删除草稿
     *
     * @param draftId 草稿Id
     * @return 空
     */
    @PostMapping("/delete")
    public Result<Void> deleteDraft(@RequestParam("draftId") long draftId) {
        boolean success = articleDraftService.deleteOriginDraft(draftId);
        return Result.successIfTrue(success);
    }

    /**
     * 删除文章
     *
     * @param draftId 草稿ID
     * @return 空
     */
    @PostMapping("/article/delete")
    public Result<Void> deleteArticle(@RequestParam("draftId") long draftId) {
        boolean success = articleDraftService.deleteArticle(draftId);
        return Result.successIfTrue(success);
    }

}
