package top.wang3.hami.core.service.notify.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.dto.notify.Info;
import top.wang3.hami.common.dto.notify.NotifyMsgDTO;
import top.wang3.hami.common.dto.notify.NotifyType;
import top.wang3.hami.common.message.NotifyRabbitReadMessage;
import top.wang3.hami.common.model.NotifyCount;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.service.interact.FollowService;
import top.wang3.hami.core.service.notify.NotifyMsgService;
import top.wang3.hami.core.service.notify.repository.NotifyMsgRepository;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotifyMsgServiceImpl implements NotifyMsgService {

    private final NotifyMsgRepository notifyMsgRepository;
    private final FollowService followService;
    private final RabbitMessagePublisher rabbitMessagePublisher;

    @Override
    public PageData<NotifyMsgDTO> listCommentNotify(PageParam param) {
        //type 1, 2
        int loginUserId = LoginUserContext.getLoginUserId();
        Page<NotifyMsgDTO> page = param.toPage();
        page = notifyMsgRepository.listCommentNotify(page, loginUserId);
        publishMessage(page, List.of(1, 2));
        return PageData.build(page);
    }

    @Override
    public PageData<NotifyMsgDTO> listLikeCollectNotify(PageParam param) {
        //type 3, 4, 5
        int loginUserId = LoginUserContext.getLoginUserId();
        Page<NotifyMsgDTO> page = param.toPage();
        page = notifyMsgRepository.listLoveNotify(page, loginUserId);
        publishMessage(page, List.of(3, 4, 5));
        return PageData.build(page);
    }

    @Override
    public PageData<NotifyMsgDTO> listFollowNotify(PageParam param) {
        //type 6
        int loginUserId = LoginUserContext.getLoginUserId();
        Page<NotifyMsgDTO> page = param.toPage();
        page = notifyMsgRepository.listFollowNotifyMsg(page, loginUserId);
        List<Integer> senders = ListMapperHandler
                .listTo(page.getRecords(), item -> item.getSender().getId());
        Map<Integer, Boolean> followed = followService.hasFollowed(loginUserId, senders);
        ListMapperHandler.forEach(page.getRecords(), (item, index) -> {
            Info sender = item.getSender();
            sender.setFollowed(followed.get(sender.getId()));
        });
        publishMessage(page, List.of(6));
        return PageData.build(page);
    }

    @Override
    public PageData<NotifyMsgDTO> listSystemMsg(PageParam param) {
        //type 0
        int loginUserId = LoginUserContext.getLoginUserId();
        Page<NotifyMsgDTO> page = param.toPage();
        page = notifyMsgRepository.listSystemNotifyMsg(page, loginUserId);
        publishMessage(page, List.of(0));
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
    public int doRead(int receiver, List<NotifyType> types) {
        List<Integer> typeIds = ListMapperHandler.listTo(types, NotifyType::getType);
        if (CollectionUtils.isEmpty(typeIds)) return 0;
        return notifyMsgRepository.updateNotifyState(receiver, typeIds);
    }

    @Override
    public boolean deleteNotify(Integer msgId) {
        return notifyMsgRepository.deleteNotifyMsg(msgId, LoginUserContext.getLoginUserId());
    }

    public void publishMessage(Page<?> page, List<Integer> types) {
        if (page != null && page.getTotal() > 0) {
            NotifyRabbitReadMessage message = new NotifyRabbitReadMessage(LoginUserContext.getLoginUserId(), types);
            rabbitMessagePublisher.publishMsg(message);
        }
    }
}
