package top.wang3.hami.core.service.notify.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.notify.NotifyMsgDTO;
import top.wang3.hami.common.dto.request.PageParam;
import top.wang3.hami.core.repository.NotifyMsgRepository;
import top.wang3.hami.core.service.notify.NotifyMsgService;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotifyMsgServiceImpl implements NotifyMsgService {

    private final NotifyMsgRepository notifyMsgRepository;


    @Override
    public PageData<NotifyMsgDTO> listCommentNotify(PageParam param) {
        int loginUserId = LoginUserContext.getLoginUserId();
        Page<NotifyMsgDTO> page = param.toPage();
        page = notifyMsgRepository.listCommentNotify(page, loginUserId);
        return PageData.build(page);
    }

    @Override
    public PageData<NotifyMsgDTO> listLikeCollectNotify(PageParam param) {
        int loginUserId = LoginUserContext.getLoginUserId();
        Page<NotifyMsgDTO> page = param.toPage();
        page = notifyMsgRepository.listLikeCollectNotify(page, loginUserId);
        return PageData.build(page);
    }

    @Override
    public PageData<NotifyMsgDTO> listFollowNotify(PageParam param) {
        int loginUserId = LoginUserContext.getLoginUserId();
        Page<NotifyMsgDTO> page = param.toPage();
        page = notifyMsgRepository.listFollowNotifyMsg(page, loginUserId);
        return PageData.build(page);
    }

    @Override
    public Map<Integer, Integer> getNoReadNotify() {
        int loginUserId = LoginUserContext.getLoginUserId();
        return notifyMsgRepository.selectNoReadNotify(loginUserId);
    }
}
