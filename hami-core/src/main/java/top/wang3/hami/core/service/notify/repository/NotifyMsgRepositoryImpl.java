package top.wang3.hami.core.service.notify.repository;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Repository;
import top.wang3.hami.common.model.NotifyCount;
import top.wang3.hami.common.model.NotifyMsg;
import top.wang3.hami.common.vo.notify.NotifyMsgVo;
import top.wang3.hami.core.mapper.NotifyMsgMapper;

import java.util.List;

@Repository
public class NotifyMsgRepositoryImpl extends ServiceImpl<NotifyMsgMapper, NotifyMsg>
        implements NotifyMsgRepository {

    @Override
    public Page<NotifyMsgVo> listCommentNotify(Page<NotifyMsgVo> page, Integer receiver) {
        if (page == null || receiver == null) return null;
        return getBaseMapper().listCommentNotify(page, receiver);
    }

    @Override
    public Page<NotifyMsgVo> listLoveNotify(Page<NotifyMsgVo> page, Integer receiver) {
        if (page == null || receiver == null) return null;
        return getBaseMapper().listLoveNotify(page, receiver);
    }

    @Override
    public Page<NotifyMsgVo> listFollowNotifyMsg(Page<NotifyMsgVo> page, Integer receiver) {
        if (page == null || receiver == null) return null;
        return getBaseMapper().listFollowNotify(page, receiver);
    }

    @Override
    public Page<NotifyMsgVo> listSystemNotifyMsg(Page<NotifyMsgVo> page, Integer receiver) {
        if (page == null || receiver == null) return null;
        return getBaseMapper().listSystemNotifyMsg(page, receiver);
    }

    @Override
    public List<NotifyCount> selectNoReadNotify(Integer receiver) {
        return getBaseMapper().selectNoReadNotify(receiver);
    }


    @Override
    public boolean saveNotifyMsg(NotifyMsg msg) {
        int rows = getBaseMapper().saveNotifyMsg(msg);
        return rows >= 1;
    }

    @Override
    public int updateNotifyState(Integer receiver, List<Integer> types) {
        UpdateWrapper<NotifyMsg> wrapper = Wrappers.update(new NotifyMsg())
                .eq("receiver", receiver)
                .in("type", types)
                .set("`state`", 1);
        return getBaseMapper().update(null, wrapper);
    }

    @Override
    public boolean deleteNotifyMsg(Integer msgId, int loginUserId) {
        return ChainWrappers.updateChain(getBaseMapper())
                .eq("id", msgId)
                .eq("receiver", loginUserId)
                .remove();
    }
}
