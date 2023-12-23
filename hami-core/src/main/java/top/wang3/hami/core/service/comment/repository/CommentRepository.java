package top.wang3.hami.core.service.comment.repository;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;
import top.wang3.hami.common.dto.comment.Reply;
import top.wang3.hami.common.model.Comment;

import java.util.List;

public interface CommentRepository extends IService<Comment> {

    List<Comment> listComment(Page<Comment> page, Integer articleId, Integer sort);

    List<Comment> listReply(Page<Comment> page, Integer articleId, Integer rootId);

    Reply listIndexReply(Integer articleId, Integer rootId);

    int deleteComment(Integer id);

    List<Comment> listCommentById(List<Integer> commentIds);

    boolean checkCommentExist(int itemId);

    Integer getCommentUser(Integer id);

    @Transactional(rollbackFor = Exception.class)
    Long batchUpdateLikes(List<Comment> comments);
}
