package top.wang3.hami.core.repository.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.wang3.hami.common.model.NotifyMsg;
import top.wang3.hami.core.mapper.NotifyMsgMapper;
import top.wang3.hami.core.repository.NotifyMsgRepository;

@Repository
public class NotifyMsgRepositoryImpl extends ServiceImpl<NotifyMsgMapper, NotifyMsg>
        implements NotifyMsgRepository {
}
