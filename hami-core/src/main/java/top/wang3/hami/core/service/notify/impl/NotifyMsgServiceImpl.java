package top.wang3.hami.core.service.notify.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.dto.notify.Info;
import top.wang3.hami.common.message.NotifyRabbitReadMessage;
import top.wang3.hami.common.model.NotifyCount;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.vo.notify.NotifyMsgVo;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.service.interact.FollowService;
import top.wang3.hami.core.service.notify.NotifyMsgService;
import top.wang3.hami.core.service.notify.repository.NotifyMsgRepository;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotifyMsgServiceImpl implements NotifyMsgService {

    private final NotifyMsgRepository notifyMsgRepository;
    private final FollowService followService;
    private final RabbitMessagePublisher rabbitMessagePublisher;
    private final TaskExecutor taskExecutor;

    @Override
    public PageData<NotifyMsgVo> listCommentNotify(PageParam param) {
        // type 1, 2
        int loginUserId = LoginUserContext.getLoginUserId();
        Page<NotifyMsgVo> page = param.toPage();
        page = notifyMsgRepository.listCommentNotify(page, loginUserId);
        publishMessage(page);
        return PageData.build(page);
    }

    @Override
    public PageData<NotifyMsgVo> listLikeCollectNotify(PageParam param) {
        // type 3, 4, 5
        int loginUserId = LoginUserContext.getLoginUserId();
        Page<NotifyMsgVo> page = param.toPage();
        page = notifyMsgRepository.listLoveNotify(page, loginUserId);
        publishMessage(page);
        return PageData.build(page);
    }

    @Override
    public PageData<NotifyMsgVo> listFollowNotify(PageParam param) {
        // type 6
        int loginUserId = LoginUserContext.getLoginUserId();
        Page<NotifyMsgVo> page = param.toPage();
        page = notifyMsgRepository.listFollowNotifyMsg(page, loginUserId);
        List<Integer> senders = ListMapperHandler
                .listTo(page.getRecords(), item -> item.getSender().getId());
        Map<Integer, Boolean> followed = followService.hasFollowed(loginUserId, senders);
        ListMapperHandler.forEach(page.getRecords(), (item, index) -> {
            Info sender = item.getSender();
            sender.setFollowed(followed.get(sender.getId()));
        });
        publishMessage(page);
        return PageData.build(page);
    }

    @Override
    public PageData<NotifyMsgVo> listSystemMsg(PageParam param) {
        // type 0
        int loginUserId = LoginUserContext.getLoginUserId();
        Page<NotifyMsgVo> page = param.toPage();
        page = notifyMsgRepository.listSystemNotifyMsg(page, loginUserId);
        publishMessage(page);
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
    public boolean deleteNotify(Integer msgId) {
        return notifyMsgRepository.deleteNotifyMsg(msgId, LoginUserContext.getLoginUserId());
    }

    public void publishMessage(Page<NotifyMsgVo> page) {
        if (page != null && page.getTotal() > 0) {
            taskExecutor.execute(() -> {
                List<NotifyMsgVo> records = page.getRecords();
                ArrayList<Integer> needsUpdate = new ArrayList<>((int) page.getSize());
                for (NotifyMsgVo record : records) {
                    if (record.getState() == Constants.ZERO) {
                        needsUpdate.add(record.getId());
                    }
                }
                if (!needsUpdate.isEmpty()) {
                    NotifyRabbitReadMessage message = new NotifyRabbitReadMessage(LoginUserContext.getLoginUserId(), needsUpdate);
                    rabbitMessagePublisher.publishMsgSync(message);
                }
            });
        }
    }
}
