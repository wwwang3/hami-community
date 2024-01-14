package top.wang3.hami.web.controller.interact;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.article.UserArticleParam;
import top.wang3.hami.common.dto.builder.UserOptionsBuilder;
import top.wang3.hami.common.dto.interact.LikeItemParam;
import top.wang3.hami.common.dto.interact.LikeType;
import top.wang3.hami.common.dto.user.UserDTO;
import top.wang3.hami.common.model.ArticleCollect;
import top.wang3.hami.common.model.LikeItem;
import top.wang3.hami.common.model.UserFollow;
import top.wang3.hami.common.vo.article.ArticleDTO;
import top.wang3.hami.core.exception.HamiServiceException;
import top.wang3.hami.core.service.article.ArticleService;
import top.wang3.hami.core.service.interact.CollectService;
import top.wang3.hami.core.service.interact.FollowService;
import top.wang3.hami.core.service.interact.LikeService;
import top.wang3.hami.core.service.user.UserService;
import top.wang3.hami.security.model.Result;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;

import java.util.Collection;
import java.util.List;

/**
 * 用户交互 关注 点赞 收藏 评论
 */
@RestController
@RequestMapping("/api/v1/interact")
@RequiredArgsConstructor
public class UserInteractController {

    private final FollowService followService;
    private final LikeService likeService;
    private final CollectService collectService;
    private final ArticleService articleService;
    private final UserService userService;



    @PostMapping("/follow")
    @RateLimit(capacity = 100, interval = 86400L, scope = RateLimit.Scope.LOGIN_USER,
            algorithm = RateLimit.Algorithm.FIXED_WINDOW)
    public Result<Void> doFollow(@RequestParam("followingId") int followingId) {
        return Result.ofTrue(followService.follow(followingId))
                .orElse("操作失败");
    }

    @PostMapping("/follow/cancel")
    @RateLimit(capacity = 100, interval = 86400L, scope = RateLimit.Scope.LOGIN_USER,
            algorithm = RateLimit.Algorithm.FIXED_WINDOW)
    public Result<Void> unFollow(@RequestParam("followingId") int followingId) {
        return Result.ofTrue(followService.unFollow(followingId))
                .orElse("操作失败");
    }


    @PostMapping("/like")
    @RateLimit(capacity = 100, interval = 86400L, scope = RateLimit.Scope.LOGIN_USER,
            algorithm = RateLimit.Algorithm.FIXED_WINDOW)
    public Result<Void> like(@RequestBody LikeItemParam param) {
        LikeType likeType = resolveLikerType(param.getItemType());
        return Result.ofTrue(likeService.doLike(param.getItemId(), likeType))
                .orElse("操作失败");
    }

    @PostMapping("/like/cancel")
    @RateLimit(capacity = 100, interval = 86400L, scope = RateLimit.Scope.LOGIN_USER,
            algorithm = RateLimit.Algorithm.FIXED_WINDOW)
    public Result<Void> cancelLike(@RequestBody LikeItemParam param) {
        return Result
                .ofTrue(likeService.cancelLike(param.getItemId(), resolveLikerType(param.getItemType())))
                .orElse("操作失败");
    }

    @PostMapping("/collect")
    @RateLimit(capacity = 100, interval = 86400L, scope = RateLimit.Scope.LOGIN_USER,
            algorithm = RateLimit.Algorithm.FIXED_WINDOW)
    public Result<Void> collect(@RequestParam("articleId") int articleId) {
        return Result
                .ofTrue(collectService.doCollect(articleId))
                .orElse("操作失败");
    }

    @PostMapping("/collect/cancel")
    @RateLimit(capacity = 100, interval = 86400L, scope = RateLimit.Scope.LOGIN_USER,
            algorithm = RateLimit.Algorithm.FIXED_WINDOW)
    public Result<Void> cancelCollect(@RequestParam("articleId") int articleId) {
        return Result
                .ofTrue(collectService.cancelCollect(articleId))
                .orElse("操作失败");
    }


    @PostMapping("/collect/query_list")
    public Result<PageData<ArticleDTO>> getUserCollectArticles(@RequestBody @Valid
                                                               UserArticleParam param) {
        Page<ArticleCollect> page = param.toPage();
        Collection<Integer> articleIds = collectService.listUserCollects(page, param.getUserId());
        List<ArticleDTO> data = articleService.listArticleDTOById(articleIds, null);
        PageData<ArticleDTO> pageData = PageData.<ArticleDTO>builder()
                .pageNum(param.getPageNum())
                .total(page.getTotal())
                .data(data)
                .build();
        return Result.successData(pageData);
    }

    @PostMapping("/like/query_list")
    public Result<PageData<ArticleDTO>> getUserLikeArticles(@RequestBody @Valid
                                                               UserArticleParam param) {
        Page<LikeItem> page = param.toPage();
        Collection<Integer> articleIds = likeService.listUserLikeArticles(page, param.getUserId());
        Collection<ArticleDTO> data = articleService.listArticleDTOById(articleIds, null);
        PageData<ArticleDTO> pageData = PageData.<ArticleDTO>builder()
                .pageNum(param.getPageNum())
                .total(page.getTotal())
                .data(data)
                .build();
        return Result.successData(pageData);
    }

    @PostMapping("/follow/following_list")
    public Result<PageData<UserDTO>> getUserFollowings(@RequestBody @Valid
                                                               UserArticleParam param) {
        Page<UserFollow> page = param.toPage();
        Collection<Integer> followings = followService.listUserFollowings(page, param.getUserId());
        Collection<UserDTO> data = userService.listAuthorInfoById(followings, UserOptionsBuilder.justInfo());
        data.forEach(d -> d.setFollowed(true));
        PageData<UserDTO> pageData = PageData.<UserDTO>builder()
                .pageNum(param.getPageNum())
                .total(page.getTotal())
                .data(data)
                .build();
        return Result.successData(pageData);
    }

    @PostMapping("/follow/follower_list")
    public Result<PageData<UserDTO>> getUserFollowers(@RequestBody @Valid
                                                               UserArticleParam param) {
        Page<UserFollow> page = param.toPage();
        Collection<Integer> followers = followService.listUserFollowers(page, param.getUserId());
        UserOptionsBuilder builder = new UserOptionsBuilder()
                .noStat();
        Collection<UserDTO> data = userService.listAuthorInfoById(followers, builder);
        PageData<UserDTO> pageData = PageData.<UserDTO>builder()
                .pageNum(param.getPageNum())
                .total(page.getTotal())
                .data(data)
                .build();
        return Result.successData(pageData);
    }

    private LikeType resolveLikerType(Byte type) {
        LikeType likeType = LikeType.of(type);
        if (likeType == null) {
            throw new HamiServiceException("不支持的类型");
        }
        return likeType;
    }

}
