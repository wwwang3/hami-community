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

/**
 * draft
 */
@RestController
@RequestMapping("/api/v1/draft")
@RequiredArgsConstructor
public class ArticleDraftController {

    private final ArticleDraftService articleDraftService;

    /**
     * 分页获取未发布文章草稿
     *
     * @param param {@link PageParam}
     * @return {@link PageData<ArticleDraftVo>}
     */
    @PostMapping("/list")
    public Result<PageData<ArticleDraftVo>> getDrafts(@RequestBody PageParam param) {
        PageData<ArticleDraftVo> articleDrafts = articleDraftService.listDraftByPage(param, Constants.ZERO);
        return Result.successData(articleDrafts);
    }

    /**
     * 分页获取发布文章草稿
     *
     * @param param {@link PageParam}
     * @return {@link PageData<ArticleDraftVo>}
     */
    @GetMapping("/articles")
    public Result<PageData<ArticleDraftVo>> getArticles(@RequestBody PageParam param) {
        PageData<ArticleDraftVo> drafts = articleDraftService.listDraftByPage(param, Constants.ONE);
        return Result.successData(drafts);
    }

    /**
     * 根据Id获取草稿
     *
     * @param draftId 草稿Id
     * @return {@link ArticleDraftVo}
     */
    @GetMapping("/{id}")
    public Result<ArticleDraftVo> getDraft(@PathVariable("id") Long draftId) {
        ArticleDraftVo draft = articleDraftService.getArticleDraftById(draftId);
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
     * @description 返回数据包含文章Id
     * @return {@link ArticleDraft}
     */
    @PostMapping("/publish")
    public Result<ArticleDraft> publishArticle(@RequestParam("draft_id") Long draftId) {
        ArticleDraft draft = articleDraftService.publishArticle(draftId);
        return Result.successIfNonNull(draft);
    }

    /**
     * 删除草稿
     *
     * @param draftId 草稿Id
     * @return {@link Void}
     */
    @PostMapping("/delete/draft")
    public Result<Void> deleteDraft(@RequestParam("draft_id") long draftId) {
        boolean success = articleDraftService.deleteDraft(draftId);
        return Result.successIfTrue(success);
    }

    /**
     * 删除文章
     *
     * @param articleId 文章Id
     * @return {@link Void}
     */
    @PostMapping("/delete/article")
    public Result<Void> deleteArticle(@RequestParam("articleId") int articleId) {
        boolean success = articleDraftService.deleteArticle(articleId);
        return Result.successIfTrue(success);
    }

}
