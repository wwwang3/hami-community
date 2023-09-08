package top.wang3.hami.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.wang3.hami.common.model.NotifyMsg;

@Mapper
public interface NotifyMsgMapper extends BaseMapper<NotifyMsg> {

    @Select("""
        select 1 from notify_msg where sender = #{sender} and receiver = #{receiver} and type = #{type};
    """)
    boolean hasExist(@Param("sender") Integer sender, @Param("receiver") Integer receiver, @Param("type") Integer type);
}