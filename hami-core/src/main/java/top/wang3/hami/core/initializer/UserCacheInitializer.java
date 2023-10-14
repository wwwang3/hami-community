package top.wang3.hami.core.initializer;


import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.dto.builder.UserOptionsBuilder;
import top.wang3.hami.core.service.user.UserService;
import top.wang3.hami.core.service.user.repository.UserRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
@Order(4)
public class UserCacheInitializer implements HamiInitializer {

    private final UserRepository userRepository;
    private final UserService userService;


    @Override
    public String getName() {
        return USER_CACHE;
    }

    @Override
    public void run() {
        cacheUser();
    }

    private void cacheUser() {
        int lastId = 0;
        while (true) {
            List<Integer> userIds = userRepository.scanUserIds(lastId, 2000);
            if (userIds == null || userIds.isEmpty()) {
                break;
            }
            userService.listAuthorInfoById(userIds, new UserOptionsBuilder());
            lastId = userIds.get(userIds.size() - 1);
        }
    }
}
