package top.wang3.hami.web.controller.interact;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.wang3.hami.common.dto.ArticleDTO;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.UserDTO;
import top.wang3.hami.common.dto.request.CommentParam;
import top.wang3.hami.common.dto.request.LikeItemParam;
import top.wang3.hami.common.dto.request.PageParam;
import top.wang3.hami.common.dto.request.UserArticleParam;
import top.wang3.hami.common.model.*;
import top.wang3.hami.core.service.article.ArticleService;
import top.wang3.hami.core.service.article.ReadingRecordService;
import top.wang3.hami.core.service.interact.UserInteractService;
import top.wang3.hami.core.service.user.UserService;
import top.wang3.hami.security.model.Result;

import java.util.List;

/**
 * 用户交互 关注 点赞 收藏 评论
 */
@RestController
@RequestMapping("/api/v1/interact")
@RequiredArgsConstructor
public class UserInteractController {

    private final UserInteractService userInteractService;

    private final ReadingRecordService readingRecordService;

    private final ArticleService articleService;

    private final UserService userService;

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

    @GetMapping("/reading_record")
    public Result<PageData<ReadingRecordDTO>> getReadingRecord(@RequestParam("pageNum") long pageNum,
                                                               @RequestParam("pageSize") long pageSize) {
        PageData<ReadingRecordDTO> pageData = readingRecordService
                .getReadingRecords(new PageParam(pageNum, pageSize));
        return Result.ofNullable(pageData)
                .orElse("还没有历史记录");
    }

    @PostMapping("/collect/query_list")
    public Result<PageData<ArticleDTO>> getUserCollectArticles(@RequestBody @Valid
                                                               UserArticleParam param) {
        Page<ArticleCollect> page = param.toPage();
        List<Integer> articleIds = userInteractService.getUserCollectArticles(page, param.getUserId());
        List<ArticleDTO> data = articleService.getArticleByIds(articleIds, null);
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
        List<Integer> articleIds = userInteractService.getUserLikesArticles(page, param.getUserId());
        List<ArticleDTO> data = articleService.getArticleByIds(articleIds, null);
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
        List<Integer> followings = userInteractService.getUserFollowings(page, param.getUserId());
        UserService.OptionsBuilder builder = new UserService.OptionsBuilder()
                .noFollowState()
                .noStat();
        List<UserDTO> data = userService.getAuthorInfoByIds(followings, builder);
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
        List<Integer> followers = userInteractService.getUserFollowers(page, param.getUserId());
        UserService.OptionsBuilder builder = new UserService.OptionsBuilder()
                .noStat();
        List<UserDTO> data = userService.getAuthorInfoByIds(followers, builder);
        PageData<UserDTO> pageData = PageData.<UserDTO>builder()
                .pageNum(param.getPageNum())
                .total(page.getTotal())
                .data(data)
                .build();
        return Result.successData(pageData);
    }

    @PostMapping("/comment/submit")
    public Result<Comment> publishComment(@RequestBody @Valid CommentParam param) {
        Comment comment = userInteractService.publishComment(param);
        return Result.ofNullable(comment)
                .orElse("发表失败");
    }

    @PostMapping("/reply/submit")
    public Result<Comment> publishReply(@RequestBody
            @Validated(value = CommentParam.Reply.class) CommentParam param) {
        Comment comment = userInteractService.publishReply(param);
        return Result.ofNullable(comment)
                .orElse("回复失败");
    }

    @PostMapping("/comment/delete")
    public Result<Void> deleteComment(@RequestParam("id") Integer id) {
        boolean success = userInteractService.deleteComment(id);
        return Result.ofTrue(success)
                .orElse("删除失败");
    }

}
