package top.wang3.hami.web.controller.comment;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wang3.hami.common.dto.CommentDTO;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.request.CommentPageParam;
import top.wang3.hami.core.service.comment.CommentService;
import top.wang3.hami.security.model.Result;

@RestController
@RequestMapping("/api/v1/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/list")
    public Result<PageData<CommentDTO>> listComment(@RequestBody CommentPageParam param) {
        PageData<CommentDTO> data = commentService.listComment(param);
        return Result.successData(data);
    }

    @PostMapping("/list/reply")
    public Result<PageData<CommentDTO>> listReply(@RequestBody CommentPageParam param) {
        PageData<CommentDTO> data = commentService.listReply(param);
        return Result.successData(data);
    }

}
