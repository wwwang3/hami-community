package top.wang3.hami.core.repository.impl;


import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Repository;
import top.wang3.hami.common.dto.Reply;
import top.wang3.hami.common.model.Comment;
import top.wang3.hami.core.mapper.CommentMapper;
import top.wang3.hami.core.repository.CommentRepository;

import java.util.List;

@Repository
public class CommentRepositoryImpl extends ServiceImpl<CommentMapper, Comment>
        implements CommentRepository {


    public static final String[] fields = {
            "id", "article_id", "user_id", "is_author",
            "ip_info", "content", "content_img", "root_id",
            "parent_id", "reply_to", "ctime"
    };

    @Override
    public List<Comment> listComment(Page<Comment> page, Integer articleId, Integer sort) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select(fields)
                .eq("article_id", articleId)
                .eq("root_id", 0)
                .orderByDesc(sort.equals(0) ? "likes" : "id")
                .list(page);
    }

    @Override
    public List<Comment> listReply(Page<Comment> page, Integer articleId, Integer rootId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select(fields)
                .eq("article_id", articleId)
                .eq("root_id", rootId)
                .orderByDesc("id")
                .list(page);
    }

    @Override
    public Reply listIndexReply(Integer rootId) {
        //获取第一页
        Page<Comment> page = new Page<>(1, 5);
        List<Comment> comments = ChainWrappers.queryChain(getBaseMapper())
                .select(fields)
                .eq("root_id", rootId)
                .orderByDesc("likes")
                .list(page);
        Reply reply = new Reply();
        reply.setComments(comments);
        reply.setTotal(page.getTotal());
        return reply;
    }

    @Override
    public List<Comment> listCommentById(List<Integer> commentIds) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select(fields)
                .in("id", commentIds)
                .list();
    }

    @Override
    public boolean increaseLikes(Integer id) {
        return ChainWrappers.updateChain(getBaseMapper())
                .setSql("likes = likes + 1")
                .eq("id", id)
                .update();
    }

    @Override
    public int deleteComment(Integer userId, Integer id) {
        UpdateWrapper<Comment> wrapper = Wrappers
                .update(new Comment())
                .eq("id", id)
                .eq("user_id", userId);
        int deleted = getBaseMapper().delete(wrapper);
        if (deleted == 0) return 0;
        UpdateWrapper<Comment> wrapper2 = Wrappers.update(new Comment())
                .eq("root_id", id);//删除对应的回复
        deleted += getBaseMapper().delete(wrapper2);
        return deleted;
    }
}
