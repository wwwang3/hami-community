package top.wang3.hami.core.service.comment.repository;


import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Repository;
import top.wang3.hami.common.dto.comment.Reply;
import top.wang3.hami.common.model.Comment;
import top.wang3.hami.core.mapper.CommentMapper;

import java.util.List;

@Repository
public class CommentRepositoryImpl extends ServiceImpl<CommentMapper, Comment>
        implements CommentRepository {


    public static final String[] fields = {
            "id", "article_id", "user_id", "ip_info",
            "content", "content_img", "root_id", "likes",
            "parent_id", "reply_to", "ctime"
    };

    @Override
    public List<Comment> listComment(Page<Comment> page, Integer articleId, Integer sort) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select(fields)
                .eq("article_id", articleId)
                .eq("root_id", 0)
                .orderByDesc(sort.equals(0) ? "likes" : "ctime")
                .list(page);
    }

    @Override
    public List<Comment> listReply(Page<Comment> page, Integer articleId, Integer rootId) {
        return listReply(page, articleId, rootId, 1);
    }

    @Override
    public Reply listIndexReply(Integer articleId, Integer rootId) {
        //获取第一页
        Page<Comment> page = new Page<>(1, 5);
        List<Comment> comments = listReply(page, articleId, rootId, 0);
        Reply reply = new Reply();
        reply.setComments(comments);
        reply.setTotal(page.getTotal());
        return reply;
    }

    private List<Comment> listReply(Page<Comment> page, Integer articleId, Integer rootId, int sort) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select(fields)
                .eq("article_id", articleId)
                .eq("root_id", rootId)
                .orderByDesc(sort == 0 ? "likes" : "ctime")
                .list(page);
    }

    @Override
    public List<Comment> listCommentById(List<Integer> commentIds) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select(fields)
                .in("id", commentIds)
                .list();
    }

    @Override
    public boolean checkCommentExist(int itemId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("id")
                .exists("id", itemId)
                .exists();
    }

    @Override
    public Integer getCommentUser(Integer id) {
        return getBaseMapper().selectCommentUserById(id);
    }

    @Override
    public int deleteComment(Integer id) {
        UpdateWrapper<Comment> wrapper = Wrappers
                .update(new Comment())
                .eq("id", id)
                .or()
                .eq("root_id", id);
        return getBaseMapper().delete(wrapper);
    }

    @Override
    public Long batchUpdateLikes(List<Comment> comments) {
        return getBaseMapper().batchUpdateLikes(comments);
    }
}
