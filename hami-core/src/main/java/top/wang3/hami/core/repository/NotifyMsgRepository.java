package top.wang3.hami.core.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.dto.notify.NotifyMsgDTO;
import top.wang3.hami.common.dto.notify.NotifyType;
import top.wang3.hami.common.model.NotifyMsg;

import java.util.Map;

public interface NotifyMsgRepository extends IService<NotifyMsg> {

    Page<NotifyMsgDTO> listCommentNotify(Page<NotifyMsgDTO> page, Integer receiver);

    Page<NotifyMsgDTO> listLikeCollectNotify(Page<NotifyMsgDTO> page, Integer receiver);

    Page<NotifyMsgDTO> listFollowNotifyMsg(Page<NotifyMsgDTO> page, Integer receiver);

    Map<Integer, Integer> selectNoReadNotify(Integer receiver);

    boolean checkExist(Integer itemId, Integer sender, Integer receiver, NotifyType type);
}
