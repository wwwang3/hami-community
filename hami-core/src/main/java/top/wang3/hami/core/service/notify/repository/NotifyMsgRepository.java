package top.wang3.hami.core.service.notify.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;
import top.wang3.hami.common.dto.notify.NotifyMsgDTO;
import top.wang3.hami.common.dto.notify.NotifyType;
import top.wang3.hami.common.model.NotifyCount;
import top.wang3.hami.common.model.NotifyMsg;

import java.util.List;

public interface NotifyMsgRepository extends IService<NotifyMsg> {

    Page<NotifyMsgDTO> listCommentNotify(Page<NotifyMsgDTO> page, Integer receiver);

    Page<NotifyMsgDTO> listLoveNotify(Page<NotifyMsgDTO> page, Integer receiver);

    Page<NotifyMsgDTO> listFollowNotifyMsg(Page<NotifyMsgDTO> page, Integer receiver);

    Page<NotifyMsgDTO> listSystemNotifyMsg(Page<NotifyMsgDTO> page, Integer receiver);

    List<NotifyCount> selectNoReadNotify(Integer receiver);

    boolean checkExist(Integer itemId, Integer sender, Integer receiver, NotifyType type);

    int updateNotifyState(Integer receiver, List<Integer> types);

    @Transactional(rollbackFor = Exception.class)
    boolean deleteNotifyMsg(Integer msgId, int loginUserId);

}
