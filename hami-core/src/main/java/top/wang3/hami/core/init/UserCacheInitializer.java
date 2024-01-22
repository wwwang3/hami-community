package top.wang3.hami.core.init;


import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.constant.TimeoutConstants;
import top.wang3.hami.common.model.User;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.mapper.UserMapper;
import top.wang3.hami.core.service.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static top.wang3.hami.core.init.InitializerEnums.USER_CACHE;

@Component
@RequiredArgsConstructor
@Order(4)
public class UserCacheInitializer implements HamiInitializer {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public InitializerEnums getName() {
        return USER_CACHE;
    }

    @Override
    public void run() {
        cacheUser();
    }

    private void cacheUser() {
        int maxId = Integer.MAX_VALUE;
        // 100页
        int page = 0;
        int batchSize = 1000;
        while (page < 100) {
            List<User> users = userMapper.scanUserDesc(maxId, batchSize);
            if (users.isEmpty()) {
                break;
            }
            Map<String, User> map = ListMapperHandler.listToMap(users,
                    item -> RedisConstants.USER_INFO + item.getUserId());
            RedisClient.cacheMultiObject(map, TimeoutConstants.USER_INFO_EXPIRE, TimeUnit.MILLISECONDS);
            ++page;
            maxId = users.get(users.size() - 1).getUserId();
        }
    }
}
