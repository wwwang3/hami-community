package top.wang3.hami.core.service.notify.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.notify.NotifyMsgDTO;
import top.wang3.hami.common.dto.request.PageParam;
import top.wang3.hami.common.model.NotifyCount;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.core.service.notify.NotifyMsgService;
import top.wang3.hami.core.service.notify.repository.NotifyMsgRepository;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotifyMsgServiceImpl implements NotifyMsgService {

    private final NotifyMsgRepository notifyMsgRepository;


    @Override
    public PageData<NotifyMsgDTO> listCommentNotify(PageParam param) {
        //type 1, 2
        int loginUserId = LoginUserContext.getLoginUserId();
        Page<NotifyMsgDTO> page = param.toPage();
        page = notifyMsgRepository.listCommentNotify(page, loginUserId);
        return PageData.build(page);
    }

    @Override
    public PageData<NotifyMsgDTO> listLikeCollectNotify(PageParam param) {
        //type 3, 4, 5
        int loginUserId = LoginUserContext.getLoginUserId();
        Page<NotifyMsgDTO> page = param.toPage();
        page = notifyMsgRepository.listLoveNotify(page, loginUserId);
        return PageData.build(page);
    }

    @Override
    public PageData<NotifyMsgDTO> listFollowNotify(PageParam param) {
        //type 6
        int loginUserId = LoginUserContext.getLoginUserId();
        Page<NotifyMsgDTO> page = param.toPage();
        page = notifyMsgRepository.listFollowNotifyMsg(page, loginUserId);
        return PageData.build(page);
    }

    @Override
    public PageData<NotifyMsgDTO> listSystemMsg(PageParam param) {
        //type 0
        int loginUserId = LoginUserContext.getLoginUserId();
        Page<NotifyMsgDTO> page = param.toPage();
        page = notifyMsgRepository.listSystemNotifyMsg(page, loginUserId);
        return PageData.build(page);
    }

    @Override
    public Map<Integer, Integer> getNoReadNotify() {
        int loginUserId = LoginUserContext.getLoginUserId();
        List<NotifyCount> notifyCounts = notifyMsgRepository.selectNoReadNotify(loginUserId);
        return ListMapperHandler.listToMap(notifyCounts,
                NotifyCount::getType, NotifyCount::getTotal);
    }

    @Override
    public boolean doRead(Integer msgId) {
        return notifyMsgRepository.updateNotifyState(msgId, LoginUserContext.getLoginUserId());
    }

    @Override
    public boolean deleteNotify(Integer msgId) {
        return notifyMsgRepository.deleteNotifyMsg(msgId, LoginUserContext.getLoginUserId());
    }
}
