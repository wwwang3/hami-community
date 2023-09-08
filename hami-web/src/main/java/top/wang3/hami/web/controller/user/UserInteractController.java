package top.wang3.hami.web.controller.user;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wang3.hami.common.dto.request.LikeItemParam;
import top.wang3.hami.core.service.common.UserInteractService;
import top.wang3.hami.security.model.Result;

/**
 * 用户交互 关注 点赞 收藏 评论
 */
@RestController
@RequestMapping("/api/v1/interact")
@RequiredArgsConstructor
public class UserInteractController {

    private final UserInteractService userInteractService;

    @PostMapping("/follow/do")
    public Result<Void> doFollow(@RequestParam("followingId") int followingId) {
        return Result.ofTrue(() -> userInteractService.follow(followingId))
                .orElse("操作失败");
    }

    @PostMapping("/follow/undo")
    public Result<Void> unFollow(@RequestParam("followingId") int followingId) {
        return Result.ofTrue(userInteractService.unFollow(followingId))
                .orElse("操作失败");
    }

    @PostMapping("/like")
    public Result<Void> like(@RequestBody LikeItemParam param) {
        return Result.ofTrue(userInteractService.like(param.getItemId(), param.getItemType()))
                .orElse("操作失败");
    }

    @PostMapping("/like/cancel")
    public Result<Void> cancelLike(@RequestBody LikeItemParam param) {
        return Result
                .ofTrue(userInteractService.cancelLike(param.getItemId(), param.getItemType()))
                .orElse("操作失败");
    }

    @PostMapping("/collect")
    public Result<Void> collect(@RequestParam("articleId") int articleId) {
        return Result
                .ofTrue(userInteractService.collect(articleId))
                .orElse("操作失败");
    }

    @PostMapping("/collect/cancel")
    public Result<Void> cancelCollect(@RequestParam("articleId") int articleId) {
        return Result
                .ofTrue(userInteractService.cancelCollect(articleId))
                .orElse("操作失败");
    }


}