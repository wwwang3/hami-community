package top.wang3.hami.core.service.comment;

import top.wang3.hami.common.dto.CommentDTO;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.ReplyDTO;
import top.wang3.hami.common.dto.request.CommentPageParam;
import top.wang3.hami.common.dto.request.CommentParam;

public interface CommentService {

    PageData<CommentDTO> listComment(CommentPageParam commentPageParam);

    PageData<CommentDTO> listReply(CommentPageParam commentPageParam);

    ReplyDTO listIndexReply(Integer rootId);

    CommentDTO publishComment(CommentParam param);

    CommentDTO publishReply(CommentParam param);

    int deleteComment(Integer id);
}
