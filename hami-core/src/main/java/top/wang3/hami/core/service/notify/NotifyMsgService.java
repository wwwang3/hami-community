package top.wang3.hami.core.service.notify;

import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.dto.notify.NotifyType;
import top.wang3.hami.common.vo.notify.NotifyMsgVo;

import java.util.List;
import java.util.Map;

public interface NotifyMsgService {

    PageData<NotifyMsgVo> listCommentNotify(PageParam param);

    PageData<NotifyMsgVo> listLikeCollectNotify(PageParam param);

    PageData<NotifyMsgVo> listFollowNotify(PageParam param);

    PageData<NotifyMsgVo> listSystemMsg(PageParam param);

    Map<Integer, Integer> getNoReadNotify();

    int doRead(int receiver, List<NotifyType> types);

    boolean deleteNotify(Integer msgId);

}
