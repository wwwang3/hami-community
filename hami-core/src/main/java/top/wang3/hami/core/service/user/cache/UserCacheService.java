package top.wang3.hami.core.service.user.cache;

import top.wang3.hami.common.model.User;

import java.util.List;

public interface UserCacheService {

    User getUserCache(Integer userId);

    List<User> listUserById(List<Integer> userIds);
}
