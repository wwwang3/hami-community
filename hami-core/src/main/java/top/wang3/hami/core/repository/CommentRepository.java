package top.wang3.hami.core.repository;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.dto.comment.CommentInfo;
import top.wang3.hami.common.dto.comment.Reply;
import top.wang3.hami.common.model.Comment;

import java.util.List;

public interface CommentRepository extends IService<Comment> {

    List<Comment> listComment(Page<Comment> page, Integer articleId, Integer sort);

    List<Comment> listReply(Page<Comment> page, Integer articleId, Integer rootId);

    Reply listIndexReply(Integer rootId);

    boolean increaseLikes(Integer id);

    int deleteComment(Integer userId, Integer id);

    List<Comment> listCommentById(List<Integer> commentIds);

    CommentInfo getCommentWithParentById(Integer id);

    boolean checkCommentExist(int itemId);
}
