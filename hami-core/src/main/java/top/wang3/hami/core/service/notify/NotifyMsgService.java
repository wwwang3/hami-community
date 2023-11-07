package top.wang3.hami.core.service.notify;

import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.dto.notify.NotifyMsgDTO;
import top.wang3.hami.common.dto.notify.NotifyType;

import java.util.List;
import java.util.Map;

public interface NotifyMsgService {

    PageData<NotifyMsgDTO> listCommentNotify(PageParam param);

    PageData<NotifyMsgDTO> listLikeCollectNotify(PageParam param);

    PageData<NotifyMsgDTO> listFollowNotify(PageParam param);

    PageData<NotifyMsgDTO> listSystemMsg(PageParam param);

    Map<Integer, Integer> getNoReadNotify();

    int doRead(int receiver, List<NotifyType> types);

    boolean deleteNotify(Integer msgId);

}
