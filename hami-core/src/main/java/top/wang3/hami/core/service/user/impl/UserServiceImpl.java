package top.wang3.hami.core.service.user.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.model.User;
import top.wang3.hami.core.mapper.UserMapper;
import top.wang3.hami.core.service.user.UserService;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {
}
