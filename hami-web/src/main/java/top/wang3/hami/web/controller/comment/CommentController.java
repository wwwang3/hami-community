package top.wang3.hami.web.controller.comment;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.comment.CommentDTO;
import top.wang3.hami.common.dto.request.CommentPageParam;
import top.wang3.hami.common.dto.request.CommentParam;
import top.wang3.hami.common.model.Comment;
import top.wang3.hami.core.service.comment.CommentService;
import top.wang3.hami.security.model.Result;

@RestController
@RequestMapping("/api/v1/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/query_list")
    public Result<PageData<CommentDTO>> listComment(@RequestBody CommentPageParam param) {
        PageData<CommentDTO> data = commentService.listComment(param);
        return Result.successData(data);
    }

    @PostMapping("/reply/query_list")
    public Result<PageData<CommentDTO>> listReply(@RequestBody CommentPageParam param) {
        PageData<CommentDTO> data = commentService.listReply(param);
        return Result.successData(data);
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

}
