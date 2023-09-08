package top.wang3.hami.core.service.common;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wang3.hami.common.model.NotifyMsg;
import top.wang3.hami.core.mapper.NotifyMsgMapper;

@Service
public class NotifyMsgService extends ServiceImpl<NotifyMsgMapper, NotifyMsg> {

    @Transactional
    public boolean saveMsg(NotifyMsg msg) {
        return super.save(msg);
    }

    public boolean checkExist(Integer sender, Integer receiver, Integer type) {
        return getBaseMapper().hasExist(sender, receiver, type);
    }
}
