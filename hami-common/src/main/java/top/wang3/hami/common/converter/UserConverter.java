package top.wang3.hami.common.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import top.wang3.hami.common.dto.request.UserProfileParam;
import top.wang3.hami.common.dto.user.LoginProfile;
import top.wang3.hami.common.dto.user.UserDTO;
import top.wang3.hami.common.dto.user.UserProfile;
import top.wang3.hami.common.dto.user.UserStat;
import top.wang3.hami.common.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mapper
public interface UserConverter {

    UserConverter INSTANCE = Mappers.getMapper(UserConverter.class);

    @Mapping(target = "followed", ignore = true)
    @Mapping(source = "user.userId", target = "userId")
    UserDTO convert(User user, UserStat stat);

    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "followings", ignore = true)
    @Mapping(target = "followers", ignore = true)
    @Mapping(target = "collects", ignore = true)
    @Mapping(source = "user.userId", target = "userId")
    LoginProfile toLoginProfile(User user);

    UserProfile toUserProfile(User user);

    default List<UserDTO> toUserDTOList(List<User> users) {
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<UserDTO> dtos = new ArrayList<>(users.size());
        for (User user : users) {
            dtos.add(toUserDTO(user));
        }
        return dtos;
    }

    @Mapping(target = "stat", ignore = true)
    @Mapping(target = "followed", ignore = true)
    UserDTO toUserDTO(User user);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "tag", ignore = true)
    @Mapping(target = "mtime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "ctime", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    User toUser(UserProfileParam param);
}
