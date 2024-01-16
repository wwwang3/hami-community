package top.wang3.hami.core.service.user.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.model.User;

import java.util.List;

public interface UserRepository extends IService<User> {

    List<User> scanUser(Page<User> page);

    User getUserById(Integer userId);

    List<User> listUserById(List<Integer> userIds);

    boolean updateUser(Integer loginUserId, User user);
}
