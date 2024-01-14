package top.wang3.hami.core.service.user;

import top.wang3.hami.common.dto.builder.UserOptionsBuilder;
import top.wang3.hami.common.vo.user.LoginProfile;
import top.wang3.hami.common.vo.user.UserVo;

import java.util.List;

public interface UserService {

    LoginProfile getLoginProfile();

    UserVo getAuthorInfoById(int userId);

    UserVo getAuthorInfoById(int userId, UserOptionsBuilder builder);

    List<UserVo> listAuthorById(List<Integer> userIds, UserOptionsBuilder builder);

}
