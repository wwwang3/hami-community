package top.wang3.hami.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.wang3.hami.common.model.User;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {


    @Select("""
        select user_id
        from user
        where user_id > #{lastUserId}
        order by user_id
        limit #{batchSize};
    """)
    List<Integer> scanUserIds(@Param("lastUserId") int lastUserId, @Param("batchSize") int batchSize);

    List<User> scanUserDesc(@Param("maxId") int maxId, @Param("batchSize") int batchSize);

    Long batchInsertUser(@Param("users") List<User> users);
}