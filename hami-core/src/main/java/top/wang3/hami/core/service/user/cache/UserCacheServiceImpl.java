package top.wang3.hami.core.service.user.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.constant.TimeoutConstants;
import top.wang3.hami.common.model.User;
import top.wang3.hami.core.cache.CacheService;
import top.wang3.hami.core.service.user.repository.UserRepository;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserCacheServiceImpl implements UserCacheService {

    private final CacheService cacheService;
    private final UserRepository userRepository;

    @Override
    public User getUserCache(Integer userId) {
        String key = RedisConstants.USER_INFO + userId;
        return cacheService.get(
                key,
                () -> userRepository.getUserById(userId),
                TimeoutConstants.USER_INFO_EXPIRE,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public List<User> listUserById(List<Integer> userIds) {
        return cacheService.multiGetById(
                RedisConstants.USER_INFO,
                userIds,
                userRepository::listUserById,
                TimeoutConstants.USER_INFO_EXPIRE,
                TimeUnit.MILLISECONDS
        );
    }
}
