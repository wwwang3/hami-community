package top.wang3.hami.core.service.user;

import org.springframework.web.multipart.MultipartFile;
import top.wang3.hami.common.dto.builder.UserOptionsBuilder;
import top.wang3.hami.common.dto.user.LoginProfile;
import top.wang3.hami.common.dto.user.UserDTO;
import top.wang3.hami.common.dto.user.UserProfile;
import top.wang3.hami.common.model.User;

import java.util.List;

public interface UserService {
    LoginProfile getLoginProfile();

    UserProfile getUserProfile();

    User getUserById(Integer userId);

    List<UserDTO> getAuthorInfoByIds(List<Integer> userIds, UserOptionsBuilder builder);

    UserDTO getAuthorInfoById(int userId);

    UserDTO getAuthorInfoById(int userId, UserOptionsBuilder builder);

    String updateAvatar(MultipartFile avatar);

    boolean updateProfile(User user);




}
