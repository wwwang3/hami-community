package top.wang3.hami.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import top.wang3.hami.common.model.Comment;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Select("""
        select 1 from comment where id = #{commentId} and deleted = 0;
    """)
    boolean isCommentExist(Integer commentId);

    @Update("""
        update comment set likes = likes + 1 where id = #{commentId};
    """)
    int updateLikes(int commentId);

}