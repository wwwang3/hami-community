package top.wang3.hami.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wang3.hami.common.dto.notify.NotifyMsgDTO;
import top.wang3.hami.common.model.NotifyMsg;

import java.util.Map;

@Mapper
public interface NotifyMsgMapper extends BaseMapper<NotifyMsg> {

    Page<NotifyMsgDTO> listCommentNotify(Page<NotifyMsgDTO> page, @Param("receiver") Integer receiver);

    Page<NotifyMsgDTO> listLikeCollectNotify(Page<NotifyMsgDTO> page, @Param("receiver") Integer receiver);

    Page<NotifyMsgDTO> listFollowNotify(Page<NotifyMsgDTO> page, @Param("receiver") Integer receiver);


    @MapKey(value = "type")
    Map<Integer, Integer> selectNoReadNotify(Integer receiver);
}