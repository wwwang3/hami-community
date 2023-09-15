package top.wang3.hami.core.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;
import top.wang3.hami.common.dto.LoginProfile;
import top.wang3.hami.common.dto.UserProfile;
import top.wang3.hami.common.model.User;

import java.util.List;

public interface UserRepository extends IService<User> {

    User getUserById(Integer userId);

    UserProfile getUserProfile(Integer userId);

    LoginProfile getLoginProfile(Integer userId);

    List<User> getUserByIds(List<Integer> userIds);

    List<Integer> scanUserIds(int lastUserId, int batchSize);

    @Transactional(rollbackFor = Exception.class)
    boolean updateAvatar(Integer loginUserId, String url);

    @Transactional(rollbackFor = Exception.class)
    boolean updateUser(Integer loginUserId, User user);

    boolean checkUserExist(Integer userId);
}
