package top.wang3.hami.core.service.user;

import top.wang3.hami.common.dto.builder.UserOptionsBuilder;
import top.wang3.hami.common.dto.user.LoginProfile;
import top.wang3.hami.common.dto.user.UserDTO;
import top.wang3.hami.common.model.User;

import java.util.Collection;
import java.util.List;

public interface UserService {
    LoginProfile getLoginProfile();

    User getUserById(Integer userId);

    Collection<UserDTO> listAuthorInfoById(Collection<Integer> userIds, UserOptionsBuilder builder);

    UserDTO getAuthorInfoById(int userId);

    UserDTO getAuthorInfoById(int userId, UserOptionsBuilder builder);

    List<User> loadUserCache(List<Integer> userIds);
}
