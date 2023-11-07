package top.wang3.hami.core.service.comment;

import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.comment.CommentDTO;
import top.wang3.hami.common.dto.comment.CommentPageParam;
import top.wang3.hami.common.dto.comment.CommentParam;
import top.wang3.hami.common.dto.comment.ReplyDTO;
import top.wang3.hami.common.model.Comment;

public interface CommentService {

    PageData<CommentDTO> listComment(CommentPageParam commentPageParam);

    PageData<CommentDTO> listReply(CommentPageParam commentPageParam);

    ReplyDTO listIndexReply(Integer articleId, Integer rootId);

    Comment publishComment(CommentParam param);

    Comment publishReply(CommentParam param);

    boolean deleteComment(Integer id);
}
