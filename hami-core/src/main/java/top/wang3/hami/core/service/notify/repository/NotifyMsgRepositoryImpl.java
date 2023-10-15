package top.wang3.hami.core.service.notify.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Repository;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.notify.NotifyMsgDTO;
import top.wang3.hami.common.dto.notify.NotifyType;
import top.wang3.hami.common.model.NotifyMsg;
import top.wang3.hami.core.mapper.NotifyMsgMapper;

import java.util.Map;

@Repository
public class NotifyMsgRepositoryImpl extends ServiceImpl<NotifyMsgMapper, NotifyMsg>
        implements NotifyMsgRepository {

    @Override
    public Page<NotifyMsgDTO> listCommentNotify(Page<NotifyMsgDTO> page, Integer receiver) {
        if (page == null || receiver == null) return null;
        return getBaseMapper().listCommentNotify(page, receiver);
    }

    @Override
    public Page<NotifyMsgDTO> listLikeCollectNotify(Page<NotifyMsgDTO> page, Integer receiver) {
        if (page == null || receiver == null) return null;
        return getBaseMapper().listLikeCollectNotify(page, receiver);
    }

    @Override
    public Page<NotifyMsgDTO> listFollowNotifyMsg(Page<NotifyMsgDTO> page, Integer receiver) {
        if (page == null || receiver == null) return null;
        return getBaseMapper().listFollowNotify(page, receiver);
    }

    @Override
    public Map<Integer, Integer> selectNoReadNotify(Integer receiver) {
        if (receiver == null) return null;
        return getBaseMapper().selectNoReadNotify(receiver);
    }

    @Override
    public boolean checkExist(Integer itemId, Integer sender, Integer receiver, NotifyType type) {
        Long count = ChainWrappers
                .queryChain(getBaseMapper())
                .eq("item_id", itemId)
                .eq("sender", receiver)
                .eq("receiver", receiver)
                .eq("type", type.getType())
                .count();
        return count != null && count > 0;
    }

    @Override
    public boolean updateNotifyState(Integer msgId, int loginUserId) {
        return ChainWrappers.updateChain(getBaseMapper())
                .set("state", Constants.ONE)
                .eq("id", msgId)
                .eq("user_id", loginUserId)
                .eq("state", Constants.ZERO)
                .update();
    }

    @Override
    public boolean deleteNotifyMsg(Integer msgId, int loginUserId) {
        return ChainWrappers.updateChain(getBaseMapper())
                .eq("id", msgId)
                .eq("user_id", loginUserId)
                .remove();
    }
}
