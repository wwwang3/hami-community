package top.wang3.hami.web.controller.article;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.ArticleDraftDTO;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.request.ArticleDraftParam;
import top.wang3.hami.common.dto.request.PageParam;
import top.wang3.hami.common.model.ArticleDraft;
import top.wang3.hami.core.service.article.ArticleDraftService;
import top.wang3.hami.core.service.common.ImageService;
import top.wang3.hami.security.model.Result;

@RestController
@RequestMapping("/api/v1/article_draft")
@RequiredArgsConstructor
public class ArticleDraftController {

    private final ImageService imageService;

    private final ArticleDraftService articleDraftService;


    @GetMapping("/drafts")
    public Result<PageData<ArticleDraftDTO>> getDrafts(@RequestParam("pageNum") long pageNum,
                                                       @RequestParam("pageSize") long pageSize) {
        PageParam param = new PageParam(pageNum, pageSize);
        PageData<ArticleDraftDTO> articleDrafts = articleDraftService.getArticleDrafts(param, Constants.ZERO);
        return Result.success(articleDrafts);
    }

    @GetMapping("/articles")
    public Result<PageData<ArticleDraftDTO>> getArticles(@RequestParam("pageNum") long pageNum,
                                                         @RequestParam("pageSize") long pageSize) {
        PageParam param = new PageParam(pageNum, pageSize);
        PageData<ArticleDraftDTO> drafts = articleDraftService.getArticleDrafts(param, Constants.ONE);
        return Result.success(drafts);
    }

    @GetMapping("/get")
    public Result<ArticleDraftDTO> getDraft(@RequestParam("draftId") long draftId) {
        ArticleDraftDTO draft = articleDraftService.getArticleDraftById(draftId);
        return Result.success(draft);
    }

    @PostMapping("/upload/pic")
    public Result<String> uploadPicture(@RequestPart("picture") MultipartFile picture) {
        String url = imageService.upload(picture, "article-picture");
        return Result.successData(url);
    }

    @PostMapping("/create")
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
