package top.wang3.hami.core.service.notify;

import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.vo.notify.NotifyMsgVo;

import java.util.Map;

public interface NotifyMsgService {

    PageData<NotifyMsgVo> listCommentNotify(PageParam param);

    PageData<NotifyMsgVo> listLikeCollectNotify(PageParam param);

    PageData<NotifyMsgVo> listFollowNotify(PageParam param);

    PageData<NotifyMsgVo> listSystemMsg(PageParam param);

    Map<Integer, Integer> getNoReadNotify();

    boolean deleteNotify(Integer msgId);

}
