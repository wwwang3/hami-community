package top.wang3.hami.core.init;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.constant.TimeoutConstants;
import top.wang3.hami.common.model.User;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
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


    @Override
    public InitializerEnums getName() {
        return USER_CACHE;
    }

    @Override
    public void run() {
        cacheUser();
    }

    private void cacheUser() {
        Page<User> page = new Page<>(1, 1000);
        int i = 1;
        // 100页
        int size = 100;
        while (i <= size) {
            List<User> users = userRepository.scanUser(page);
            Map<String, User> map = ListMapperHandler.listToMap(users,
                    item -> RedisConstants.USER_INFO + item.getUserId());
            RedisClient.cacheMultiObject(map, TimeoutConstants.USER_INFO_EXPIRE, TimeUnit.MILLISECONDS);
            i++;
            page.setCurrent(i);
            page.setRecords(null);
            page.setSearchCount(false);
            if (!page.hasNext()) {
                break;
            }
        }
    }
}
