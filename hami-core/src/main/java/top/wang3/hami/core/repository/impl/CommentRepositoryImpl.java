package top.wang3.hami.core.repository.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.wang3.hami.common.model.Comment;
import top.wang3.hami.core.mapper.CommentMapper;
import top.wang3.hami.core.repository.CommentRepository;

@Repository
public class CommentRepositoryImpl extends ServiceImpl<CommentMapper, Comment> implements CommentRepository {


}
