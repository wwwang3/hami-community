package top.wang3.hami.core.init;


import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.wang3.hami.core.service.user.UserService;
import top.wang3.hami.core.service.user.repository.UserRepository;

import java.util.List;

import static top.wang3.hami.core.init.InitializerEnums.USER_CACHE;

@Component
@RequiredArgsConstructor
@Order(4)
public class UserCacheInitializer implements HamiInitializer {

    private final UserRepository userRepository;
    private final UserService userService;


    @Override
    public InitializerEnums getName() {
        return USER_CACHE;
    }

    @Override
    public void run() {
        cacheUser();
    }

    private void cacheUser() {
        int lastId = 0;
        while (true) {
            List<Integer> userIds = userRepository.scanUserIds(lastId, 1000);
            if (userIds == null || userIds.isEmpty()) {
                break;
            }
            userService.loadUserCache(userIds);
            lastId = userIds.get(userIds.size() - 1);
        }
    }
}
