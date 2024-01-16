package top.wang3.hami.core.service.notify.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import top.wang3.hami.common.model.NotifyCount;
import top.wang3.hami.common.model.NotifyMsg;
import top.wang3.hami.common.vo.notify.NotifyMsgVo;

import java.util.List;

public interface NotifyMsgRepository extends IService<NotifyMsg> {

    Page<NotifyMsgVo> listCommentNotify(Page<NotifyMsgVo> page, Integer receiver);

    Page<NotifyMsgVo> listLoveNotify(Page<NotifyMsgVo> page, Integer receiver);

    Page<NotifyMsgVo> listFollowNotifyMsg(Page<NotifyMsgVo> page, Integer receiver);

    Page<NotifyMsgVo> listSystemNotifyMsg(Page<NotifyMsgVo> page, Integer receiver);

    List<NotifyCount> selectNoReadNotify(Integer receiver);

    @CanIgnoreReturnValue
    boolean saveNotifyMsg(NotifyMsg msg);

    @CanIgnoreReturnValue
    int updateNotifyState(Integer receiver, List<Integer> types);

    boolean deleteNotifyMsg(Integer msgId, int loginUserId);

}
