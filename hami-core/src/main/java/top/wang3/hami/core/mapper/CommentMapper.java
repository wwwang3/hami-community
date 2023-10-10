package top.wang3.hami.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import top.wang3.hami.common.model.Comment;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Select("""
        select count(id) from comment where id = #{commentId} and deleted = 0;
    """)
    boolean isCommentExist(Integer commentId);

    @Select("""
        select user_id from comment where id = #{id} and deleted = 0;
    """)
    Integer selectCommentUserById(Integer id);

}