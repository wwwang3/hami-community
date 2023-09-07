package top.wang3.hami.core.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.wang3.hami.common.dto.LoginProfile;
import top.wang3.hami.common.dto.SimpleUserDTO;
import top.wang3.hami.common.dto.UserProfile;
import top.wang3.hami.common.model.User;

import java.util.List;

public interface UserService extends IService<User> {
    LoginProfile getLoginProfile();

    UserProfile getUserProfile();

    String updateAvatar(MultipartFile avatar);

    @Transactional(rollbackFor = Exception.class)
    boolean updateProfile(User user);

    List<SimpleUserDTO> getAuthorInfoByIds(List<Integer> userIds);

}
