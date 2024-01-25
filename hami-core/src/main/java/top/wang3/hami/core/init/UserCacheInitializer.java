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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static top.wang3.hami.core.init.InitializerEnums.USER_CACHE;

@Component
@RequiredArgsConstructor
@Order(4)
public class UserCacheInitializer implements HamiInitializer {

    private final UserMapper userMapper;

    @Override
    public InitializerEnums getName() {
        return USER_CACHE;
    }

    @Override
    public void run() {
        cacheUser();
    }

    @Override
    public boolean async() {
        return true;
    }

    private void cacheUser() {
        ListMapperHandler.scanDesc(
                Integer.MAX_VALUE,
                100,
                1000,
                userMapper::scanUserDesc,
                users -> {
                    Map<String, User> map = ListMapperHandler.listToMap(users,
                            item -> RedisConstants.USER_INFO + item.getUserId());
                    RedisClient.cacheMultiObject(map, TimeoutConstants.USER_INFO_EXPIRE, TimeUnit.MILLISECONDS);
                },
                User::getUserId
        );
    }
}
