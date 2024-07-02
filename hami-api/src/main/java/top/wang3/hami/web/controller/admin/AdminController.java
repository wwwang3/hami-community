package top.wang3.hami.web.controller.admin;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wang3.hami.common.dto.ArticleDraftPageParam;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.model.ArticleDraft;
import top.wang3.hami.common.model.Bulletin;
import top.wang3.hami.common.model.SiteStat;
import top.wang3.hami.common.vo.article.ArticleDraftVo;
import top.wang3.hami.core.service.admin.ArticleAdminService;
import top.wang3.hami.core.service.stat.CountService;
import top.wang3.hami.core.service.system.BulletinService;
import top.wang3.hami.security.model.Result;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ArticleAdminService articleAdminService;
    private final CountService countService;
    private final BulletinService bulletinService;

    @PostMapping("/content/review")
    public Result<Void> review(@RequestBody ArticleDraft draft,
                               @RequestParam(value = "msg", required = false) String msg) {
        articleAdminService.reviewArticle(draft, msg);
        return Result.success();
    }

    @PostMapping("/content/article/list")
    public Result<PageData<ArticleDraftVo>> listReviewDraft(@RequestBody @Valid ArticleDraftPageParam param) {
        PageData<ArticleDraftVo> data = articleAdminService.listReviewDraft(param);
        return Result.successData(data);
    }

    @GetMapping("/stat/site")
    public Result<SiteStat> getSiteStat() {
        SiteStat stat = countService.getSiteStat();
        return Result.successData(stat);
    }

    @PostMapping("/bulletin/publish")
    public Result<Bulletin> publishBulletin(@RequestBody @Valid Bulletin bulletin) {
        Bulletin res = bulletinService.publishBulletin(bulletin);
        return Result.successData(res);
    }

    @PostMapping("/bulletin/list")
    public Result<PageData<Bulletin>> listBulletin(@RequestBody @Valid PageParam param) {
        PageData<Bulletin> data = bulletinService.listBulletinByPage(param);
        return Result.successData(data);
    }

    @PostMapping("/bulletin/delete/{id}")
    public Result<PageData<Bulletin>> deletedBulletin(@PathVariable("id") long id) {
        boolean success = bulletinService.deleteBulletin(id);
        return Result.successIfTrue(success);
    }

}
