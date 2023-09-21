package top.wang3.hami.core.service.user.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.converter.UserConverter;
import top.wang3.hami.common.dto.builder.UserOptionsBuilder;
import top.wang3.hami.common.dto.user.LoginProfile;
import top.wang3.hami.common.dto.user.UserDTO;
import top.wang3.hami.common.dto.user.UserProfile;
import top.wang3.hami.common.dto.user.UserStat;
import top.wang3.hami.common.model.User;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.annotation.CostLog;
import top.wang3.hami.core.mapper.UserMapper;
import top.wang3.hami.core.repository.UserRepository;
import top.wang3.hami.core.service.common.ImageService;
import top.wang3.hami.core.service.interact.UserInteractService;
import top.wang3.hami.core.service.stat.CountService;
import top.wang3.hami.core.service.user.UserService;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    ImageService imageService;

    private UserService self;
    private final UserRepository repository;
    private final UserInteractService userInteractService;
    private final CountService countService;

    public UserServiceImpl(UserRepository repository,
                           UserInteractService userInteractService,
                           CountService countService) {
        this.repository = repository;
        this.userInteractService = userInteractService;
        this.countService = countService;
    }

    @Autowired
    @Lazy
    public void setSelf(UserService self) {
        this.self = self;
    }

    @Override
    public LoginProfile getLoginProfile() {
        int loginUserId = LoginUserContext.getLoginUserId();
        User user = self.getUserInfo(loginUserId);
        LoginProfile loginProfile = UserConverter.INSTANCE.toLoginProfile(user);
        //获取登录用户点赞的文章数
        Integer likes = userInteractService.getUserLikeCount(loginUserId);
        loginProfile.setLikes(likes);
        //获取登录用户收藏的文章数
        Integer collects = userInteractService.getUserCollectCount(loginUserId);
        loginProfile.setCollects(collects);
        //获取登录用户关注的用户数
        Integer followings = userInteractService.getUserFollowingCount(loginUserId);
        Integer followers = userInteractService.getUserFollowerCount(loginUserId);
        loginProfile.setFollowers(followers);
        loginProfile.setFollowings(followings);
        return loginProfile;
    }

    @Override
    public UserProfile getUserProfile() {
        int userId = LoginUserContext.getLoginUserId();
        User user = self.getUserInfo(userId);
        return UserConverter.INSTANCE.toUserProfile(user);
    }

    @Override
    public User getUserInfo(Integer userId) {
        if (userId == null || userId < 0) {
            throw new IllegalArgumentException("参数异常");
        }
        String redisKey = Constants.USER_INFO + userId;
        User user;
        if (!RedisClient.exist(redisKey)) {
            user = repository.getUserById(userId);
            if (user == null) {
                RedisClient.setCacheObject(redisKey, "", 10, TimeUnit.SECONDS);
            } else{
                RedisClient.setCacheObject(redisKey, user, 24, TimeUnit.HOURS);
            }
        } else {
            user = RedisClient.getCacheObject(redisKey);
        }
        return user;
    }

    @CostLog
    @Override
    public List<UserDTO> getAuthorInfoByIds(List<Integer> userIds, UserOptionsBuilder builder) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        List<String> keys = ListMapperHandler.listTo(userIds, id -> Constants.USER_INFO + id);
        List<User> users = RedisClient.getMultiCacheObject(keys, (key, index) -> {
           return self.getUserInfo(userIds.get(index));
        });
        List<UserDTO> dtos = UserConverter.INSTANCE.toUserDTOList(users);
        if (builder == null || builder.stat) {
            //查询用户的粉丝数据
            buildUserStat(dtos);
        }
        //查询关注状态
        if (builder == null || builder.follow) {
            buildFollowState(dtos);
        }
        return dtos;
    }

    @Override
    public UserDTO getAuthorInfoById(int userId) {
        User user = self.getUserInfo(userId);
        UserDTO dto = UserConverter.INSTANCE.toUserDTO(user);
        UserStat stat = countService.getUserStatById(userId);
        dto.setStat(stat);
        buildFollowState(dto);
        return dto;
    }

    @Override
    public String updateAvatar(MultipartFile avatar) {
        //更新头像, 登录后才能上传
        int loginUserId = LoginUserContext.getLoginUserId();
        String url = imageService.upload(avatar, "avatar", th -> th.size(120, 120));
        boolean success = repository.updateAvatar(loginUserId, url);
        if (success) {
            deleteUserCache(loginUserId);
        }
        return url;
    }

    @Override
    public boolean updateProfile(User user) {
        int loginUserId = LoginUserContext.getLoginUserId();
        boolean success = repository.updateUser(loginUserId, user);
        if (success) {
            deleteUserCache(loginUserId);
        }
        return success;
    }


    private void buildUserStat(List<UserDTO> userDTOS) {
        //用户数据
        List<Integer> userIds = ListMapperHandler.listTo(userDTOS, UserDTO::getUserId);
        List<UserStat> stats = countService.getUserStatByUserIds(userIds);
        ListMapperHandler.doAssemble(userDTOS, UserDTO::getUserId, stats, UserStat::getUserId, UserDTO::setStat);
    }

    private void buildFollowState(List<UserDTO> userDTOS) {
        LoginUserContext.getOptLoginUserId()
                .ifPresent(id -> {
                    //待判定的用户
                    List<Integer> followings = ListMapperHandler.listTo(userDTOS, UserDTO::getUserId);
                    Map<Integer, Boolean> followed = userInteractService.hasFollowed(id, followings);
                    if (followed.isEmpty()) return;
                    ListMapperHandler.doAssemble(userDTOS, UserDTO::getUserId, followed,
                            UserDTO::setFollowed);
                });
    }

    private void buildFollowState(UserDTO userDTO) {
        LoginUserContext.getOptLoginUserId()
                .ifPresent((loginUserId) -> {
                    userDTO.setFollowed(userInteractService.hasFollowed(loginUserId, userDTO.getUserId()));
                });
    }

    private void deleteUserCache(Integer userId) {
        String redisKey = "#userinfo:" + userId;
        RedisClient.deleteObject(redisKey);
    }
}
