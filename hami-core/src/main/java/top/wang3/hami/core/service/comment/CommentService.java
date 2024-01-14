package top.wang3.hami.core.service.comment;

import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.comment.CommentPageParam;
import top.wang3.hami.common.dto.comment.CommentParam;
import top.wang3.hami.common.model.Comment;
import top.wang3.hami.common.vo.comment.CommentVo;
import top.wang3.hami.common.vo.comment.ReplyVo;

public interface CommentService {

    PageData<CommentVo> listComment(CommentPageParam commentPageParam);

    PageData<CommentVo> listReply(CommentPageParam commentPageParam);

    ReplyVo listIndexReply(Integer articleId, Integer rootId);

    Comment publishComment(CommentParam param);

    Comment publishReply(CommentParam param);

    boolean deleteComment(Integer id);
}
