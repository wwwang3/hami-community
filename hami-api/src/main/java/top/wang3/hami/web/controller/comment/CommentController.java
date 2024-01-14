package top.wang3.hami.web.controller.comment;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.comment.CommentPageParam;
import top.wang3.hami.common.dto.comment.CommentParam;
import top.wang3.hami.common.model.Comment;
import top.wang3.hami.common.vo.comment.CommentVo;
import top.wang3.hami.core.service.comment.CommentService;
import top.wang3.hami.security.model.Result;

@RestController
@RequestMapping("/api/v1/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/query_list")
    public Result<PageData<CommentVo>> listComment(@RequestBody
                                                    @Valid CommentPageParam param) {
        PageData<CommentVo> data = commentService.listComment(param);
        return Result.successData(data);
    }

    @PostMapping("/reply/query_list")
    public Result<PageData<CommentVo>> listReply(@RequestBody
                                                  @Validated(value = CommentPageParam.Reply.class)
                                                  CommentPageParam param) {
        PageData<CommentVo> data = commentService.listReply(param);
        return Result.successData(data);
    }

    @PostMapping("/submit")
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

    @PostMapping("/delete")
    public Result<Void> deleteComment(@RequestParam("id") Integer id) {
        boolean success = commentService.deleteComment(id);
        return Result.ofTrue(success)
                .orElse("删除失败");
    }

}
