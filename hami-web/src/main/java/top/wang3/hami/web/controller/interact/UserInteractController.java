package top.wang3.hami.web.controller.interact;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.article.ArticleDTO;
import top.wang3.hami.common.dto.builder.UserOptionsBuilder;
import top.wang3.hami.common.dto.request.CommentParam;
import top.wang3.hami.common.dto.request.LikeItemParam;
import top.wang3.hami.common.dto.request.PageParam;
import top.wang3.hami.common.dto.request.UserArticleParam;
import top.wang3.hami.common.dto.user.UserDTO;
import top.wang3.hami.common.enums.LikeType;
import top.wang3.hami.common.model.*;
import top.wang3.hami.core.exception.ServiceException;
import top.wang3.hami.core.service.article.ArticleService;
import top.wang3.hami.core.service.comment.CommentService;
import top.wang3.hami.core.service.interact.CollectService;
import top.wang3.hami.core.service.interact.FollowService;
import top.wang3.hami.core.service.interact.LikeService;
import top.wang3.hami.core.service.interact.ReadingRecordService;
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

    private final FollowService followService;
    private final LikeService likeService;
    private final CollectService collectService;
    private final CommentService commentService;

    private final ReadingRecordService readingRecordService;

    private final ArticleService articleService;

    private final UserService userService;

    @PostMapping("/follow")
    public Result<Void> doFollow(@RequestParam("followingId") int followingId) {
        return Result.ofTrue(followService.follow(followingId))
                .orElse("操作失败");
    }

    @PostMapping("/follow/cancel")
    public Result<Void> unFollow(@RequestParam("followingId") int followingId) {
        return Result.ofTrue(followService.unFollow(followingId))
                .orElse("操作失败");
    }

    @PostMapping("/like")
    public Result<Void> like(@RequestBody LikeItemParam param) {
        LikeType likeType = resolveLikerType(param.getItemType());
        return Result.ofTrue(likeService.doLike(param.getItemId(), likeType))
                .orElse("操作失败");
    }

    @PostMapping("/like/cancel")
    public Result<Void> cancelLike(@RequestBody LikeItemParam param) {
        return Result
                .ofTrue(likeService.cancelLike(param.getItemId(), resolveLikerType(param.getItemType())))
                .orElse("操作失败");
    }

    @PostMapping("/collect")
    public Result<Void> collect(@RequestParam("articleId") int articleId) {
        return Result
                .ofTrue(collectService.doCollect(articleId))
                .orElse("操作失败");
    }

    @PostMapping("/collect/cancel")
    public Result<Void> cancelCollect(@RequestParam("articleId") int articleId) {
        return Result
                .ofTrue(collectService.cancelCollect(articleId))
                .orElse("操作失败");
    }

    @PostMapping("/reading_record/query_list")
    public Result<PageData<ReadingRecordDTO>> getReadingRecord(@RequestBody @Valid PageParam param) {
        PageData<ReadingRecordDTO> pageData = readingRecordService
                .listReadingRecords(param);
        return Result.ofNullable(pageData)
                .orElse("还没有历史记录");
    }

    @PostMapping("/collect/query_list")
    public Result<PageData<ArticleDTO>> getUserCollectArticles(@RequestBody @Valid
                                                               UserArticleParam param) {
        Page<ArticleCollect> page = param.toPage();
        List<Integer> articleIds = collectService.listUserCollects(page, param.getUserId());
        List<ArticleDTO> data = articleService.listArticleById(articleIds, null);
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
        List<Integer> articleIds = likeService.listUserLikeArticles(page, param.getUserId());
        List<ArticleDTO> data = articleService.listArticleById(articleIds, null);
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
        List<Integer> followings = followService.listUserFollowings(page, param.getUserId());
        List<UserDTO> data = userService.getAuthorInfoByIds(followings, UserOptionsBuilder.justInfo());
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
        List<Integer> followers = followService.listUserFollowers(page, param.getUserId());
        UserOptionsBuilder builder = new UserOptionsBuilder()
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
        Comment comment = commentService.publishComment(param);
        return Result.ofNullable(comment)
                .orElse("发表失败");
    }

    @PostMapping("/reply/submit")
    public Result<Comment> publishReply(@RequestBody
            @Validated(value = CommentParam.Reply.class) CommentParam param) {
        Comment comment = commentService.publishReply(param);
        return Result.ofNullable(comment)
                .orElse("回复失败");
    }

    @PostMapping("/comment/delete")
    public Result<Void> deleteComment(@RequestParam("id") Integer id) {
        boolean success = commentService.deleteComment(id);
        return Result.ofTrue(success)
                .orElse("删除失败");
    }

    private LikeType resolveLikerType(Byte type) {
        LikeType likeType = LikeType.of(type);
        if (likeType == null) {
            throw new ServiceException("不支持的类型");
        }
        return likeType;
    }

}
