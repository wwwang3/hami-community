package top.wang3.hami.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wang3.hami.common.dto.notify.NotifyMsgDTO;
import top.wang3.hami.common.model.NotifyCount;
import top.wang3.hami.common.model.NotifyMsg;

import java.util.List;

@Mapper
public interface NotifyMsgMapper extends BaseMapper<NotifyMsg> {

    Page<NotifyMsgDTO> listCommentNotify(Page<NotifyMsgDTO> page, @Param("receiver") Integer receiver);

    Page<NotifyMsgDTO> listLoveNotify(Page<NotifyMsgDTO> page, @Param("receiver") Integer receiver);

    Page<NotifyMsgDTO> listFollowNotify(Page<NotifyMsgDTO> page, @Param("receiver") Integer receiver);

    Page<NotifyMsgDTO> listSystemNotifyMsg(Page<NotifyMsgDTO> page, @Param("receiver") Integer receiver);


    List<NotifyCount> selectNoReadNotify(Integer receiver);

}