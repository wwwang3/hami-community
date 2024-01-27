package top.wang3.hami.web.controller.interact;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.article.UserPageParam;
import top.wang3.hami.common.dto.builder.UserOptionsBuilder;
import top.wang3.hami.common.dto.interact.LikeItemParam;
import top.wang3.hami.common.dto.interact.LikeType;
import top.wang3.hami.common.model.ArticleCollect;
import top.wang3.hami.common.model.LikeItem;
import top.wang3.hami.common.model.UserFollow;
import top.wang3.hami.common.vo.article.ArticleVo;
import top.wang3.hami.common.vo.user.UserVo;
import top.wang3.hami.core.exception.HamiServiceException;
import top.wang3.hami.core.service.article.ArticleService;
import top.wang3.hami.core.service.interact.CollectService;
import top.wang3.hami.core.service.interact.FollowService;
import top.wang3.hami.core.service.interact.LikeService;
import top.wang3.hami.core.service.user.UserService;
import top.wang3.hami.security.model.Result;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;
import top.wang3.hami.web.annotation.Public;

import java.util.List;

/**
 * interact
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


    /**
     * 用户关注
     *
     * @param followingId 关注的用户ID
     * @return 空
     */
    @PostMapping("/follow")
    @RateLimit(capacity = 100, interval = 86400L, scope = RateLimit.Scope.LOGIN_USER,
            algorithm = RateLimit.Algorithm.FIXED_WINDOW)
    public Result<Void> doFollow(@RequestParam("followingId") int followingId) {
        return Result.ofTrue(followService.follow(followingId))
                .orElse("操作失败");
    }

    /**
     * 取消关注
     *
     * @param followingId 关注的用户ID
     * @return 空
     */
    @PostMapping("/follow/cancel")
    @RateLimit(capacity = 100, interval = 86400L, scope = RateLimit.Scope.LOGIN_USER,
            algorithm = RateLimit.Algorithm.FIXED_WINDOW)
    public Result<Void> unFollow(@RequestParam("followingId") int followingId) {
        return Result.ofTrue(followService.unFollow(followingId))
                .orElse("操作失败");
    }

    /**
     * 点赞
     *
     * @param param {@link LikeItemParam}
     * @return 空
     */
    @PostMapping("/like")
    @RateLimit(capacity = 100, interval = 86400L, scope = RateLimit.Scope.LOGIN_USER,
            algorithm = RateLimit.Algorithm.FIXED_WINDOW)
    public Result<Void> like(@RequestBody LikeItemParam param) {
        LikeType likeType = resolveLikerType(param.getItemType());
        return Result.ofTrue(likeService.doLike(param.getItemId(), likeType))
                .orElse("操作失败");
    }

    /**
     * 取消点赞
     *
     * @param param {@link LikeItemParam}
     * @return 空
     */
    @PostMapping("/like/cancel")
    @RateLimit(capacity = 100, interval = 86400L, scope = RateLimit.Scope.LOGIN_USER,
            algorithm = RateLimit.Algorithm.FIXED_WINDOW)
    public Result<Void> cancelLike(@RequestBody @Valid LikeItemParam param) {
        return Result
                .ofTrue(likeService.cancelLike(param.getItemId(), resolveLikerType(param.getItemType())))
                .orElse("操作失败");
    }

    /**
     * 文章收藏
     *
     * @param articleId 文章Id
     * @return 空
     */
    @PostMapping("/collect")
    @RateLimit(capacity = 100, interval = 86400L, scope = RateLimit.Scope.LOGIN_USER,
            algorithm = RateLimit.Algorithm.FIXED_WINDOW)
    public Result<Void> collect(@RequestParam("articleId") int articleId) {
        return Result
                .ofTrue(collectService.doCollect(articleId))
                .orElse("操作失败");
    }

    /**
     * 取消文章收藏
     *
     * @param articleId 文章Id
     * @return 空
     */
    @PostMapping("/collect/cancel")
    @RateLimit(capacity = 100, interval = 86400L, scope = RateLimit.Scope.LOGIN_USER,
            algorithm = RateLimit.Algorithm.FIXED_WINDOW)
    public Result<Void> cancelCollect(@RequestParam("articleId") int articleId) {
        return Result
                .ofTrue(collectService.cancelCollect(articleId))
                .orElse("操作失败");
    }

    /**
     * 查询用户收藏文章列表
     *
     * @param param {@link UserPageParam}
     * @return {@link PageData<ArticleVo>}
     */
    @Public
    @PostMapping("/collect/query_list")
    public Result<PageData<ArticleVo>> getUserCollectArticles(@RequestBody @Valid
                                                              UserPageParam param) {
        Page<ArticleCollect> page = param.toPage();
        List<Integer> articleIds = collectService.listUserCollects(page, param.getUserId());
        List<ArticleVo> data = articleService.listArticleVoById(articleIds, null);
        PageData<ArticleVo> pageData = PageData.<ArticleVo>builder()
                .current(param.getCurrent())
                .total(page.getTotal())
                .data(data)
                .build();
        return Result.successData(pageData);
    }

    /**
     * 查询用户点赞文章列表
     *
     * @param param {@link UserPageParam}
     * @return {@link PageData<ArticleVo>}
     */
    @Public
    @PostMapping("/like/query_list")
    public Result<PageData<ArticleVo>> getUserLikeArticles(@RequestBody @Valid
                                                           UserPageParam param) {
        Page<LikeItem> page = param.toPage();
        List<Integer> articleIds = likeService.listUserLikeArticles(page, param.getUserId());
        List<ArticleVo> data = articleService.listArticleVoById(articleIds, null);
        PageData<ArticleVo> pageData = PageData.<ArticleVo>builder()
                .current(param.getCurrent())
                .total(page.getTotal())
                .data(data)
                .build();
        return Result.successData(pageData);
    }

    /**
     * 查询用户关注列表
     *
     * @param param {@link UserPageParam}
     * @return {@link PageData<UserVo>}
     */
    @Public
    @PostMapping("/follow/following_list")
    public Result<PageData<UserVo>> getUserFollowings(@RequestBody @Valid
                                                      UserPageParam param) {
        Page<UserFollow> page = param.toPage();
        List<Integer> followings = followService.listUserFollowings(page, param.getUserId());
        List<UserVo> data = userService.listAuthorById(followings, UserOptionsBuilder.justInfo());
        data.forEach(d -> d.setFollowed(true));
        PageData<UserVo> pageData = PageData.<UserVo>builder()
                .current(param.getCurrent())
                .total(page.getTotal())
                .data(data)
                .build();
        return Result.successData(pageData);
    }

    /**
     * 查询用户粉丝列表
     *
     * @param param {@link UserPageParam}
     * @return {@link PageData<UserVo>}
     */
    @Public
    @PostMapping("/follow/follower_list")
    public Result<PageData<UserVo>> getUserFollowers(@RequestBody @Valid
                                                     UserPageParam param) {
        Page<UserFollow> page = param.toPage();
        List<Integer> followers = followService.listUserFollowers(page, param.getUserId());
        UserOptionsBuilder builder = new UserOptionsBuilder()
                .noStat();
        List<UserVo> data = userService.listAuthorById(followers, builder);
        PageData<UserVo> pageData = PageData.<UserVo>builder()
                .current(param.getCurrent())
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
