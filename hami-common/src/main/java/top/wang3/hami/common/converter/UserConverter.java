package top.wang3.hami.common.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import top.wang3.hami.common.dto.LoginProfile;
import top.wang3.hami.common.dto.UserDTO;
import top.wang3.hami.common.dto.UserProfile;
import top.wang3.hami.common.dto.UserStat;
import top.wang3.hami.common.dto.request.UserProfileParam;
import top.wang3.hami.common.model.User;

@Mapper
public interface UserConverter {

    UserConverter INSTANCE = Mappers.getMapper(UserConverter.class);

    @Mapping(source = "user.userId", target = "userId")
    UserDTO convert(User user, UserStat stat);

    @Mapping(source = "user.userId", target = "userId")
    LoginProfile toLoginProfile(User user);

    UserProfile toUserProfile(User user);

    User toUser(UserProfileParam param);
}
