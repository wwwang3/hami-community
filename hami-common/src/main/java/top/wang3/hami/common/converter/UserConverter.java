package top.wang3.hami.common.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import top.wang3.hami.common.dto.user.UserProfileParam;
import top.wang3.hami.common.model.User;
import top.wang3.hami.common.vo.user.LoginProfile;
import top.wang3.hami.common.vo.user.UserVo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mapper
public interface UserConverter {

    UserConverter INSTANCE = Mappers.getMapper(UserConverter.class);

    @Mapping(target = "stat", ignore = true)
    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "followings", ignore = true)
    @Mapping(target = "followers", ignore = true)
    @Mapping(target = "collects", ignore = true)
    LoginProfile toLoginProfile(User user);

    default List<UserVo> toUserVoList(List<User> users) {
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<UserVo> dtos = new ArrayList<>(users.size());
        for (User user : users) {
            dtos.add(toUserVo(user));
        }
        return dtos;
    }

    @Mapping(target = "stat", ignore = true)
    @Mapping(target = "followed", ignore = true)
    UserVo toUserVo(User user);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "tag", ignore = true)
    @Mapping(target = "mtime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "ctime", ignore = true)
    @Mapping(target = "username", ignore = true)
    User toUser(UserProfileParam param);
}
