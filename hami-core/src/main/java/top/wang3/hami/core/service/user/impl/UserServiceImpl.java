package top.wang3.hami.core.service.user.impl;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.converter.UserConverter;
import top.wang3.hami.common.dto.builder.UserOptionsBuilder;
import top.wang3.hami.common.dto.user.LoginProfile;
import top.wang3.hami.common.dto.user.UserDTO;
import top.wang3.hami.common.dto.user.UserStat;
import top.wang3.hami.common.dto.interact.LikeType;
import top.wang3.hami.common.message.UserRabbitMessage;
import top.wang3.hami.common.model.User;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RandomUtils;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.annotation.CostLog;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.service.account.repository.AccountRepository;
import top.wang3.hami.core.service.common.ImageService;
import top.wang3.hami.core.service.interact.CollectService;
import top.wang3.hami.core.service.interact.FollowService;
import top.wang3.hami.core.service.interact.LikeService;
import top.wang3.hami.core.service.stat.CountService;
import top.wang3.hami.core.service.user.UserService;
import top.wang3.hami.core.service.user.repository.UserRepository;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final ImageService imageService;
    private final FollowService followService;
    private final CountService countService;
    private final LikeService likeService;
    private final CollectService collectService;
    private final UserRepository userRepository;
    private final RabbitMessagePublisher rabbitMessagePublisher;

    @Resource
    TransactionTemplate transactionTemplate;

    @Override
    public LoginProfile getLoginProfile() {
        int loginUserId = LoginUserContext.getLoginUserId();
        User user = this.getUserById(loginUserId);
        LoginProfile loginProfile = UserConverter.INSTANCE.toLoginProfile(user);
        buildLoginUserStat(loginProfile, loginUserId);
        return loginProfile;
    }

    @Override
    public User getUserById(Integer userId) {
        if (userId == null || userId < 0) {
            throw new IllegalArgumentException("参数异常");
        }
        String redisKey = RedisConstants.USER_INFO + userId;
        User user = RedisClient.getCacheObject(redisKey);
        if (user == null) {
            user = loadUserCache(redisKey, userId);
        } else if (user.getUserId() == null) {
            return null;
        }
        return user;
    }

    @CostLog
    @Override
    public Collection<UserDTO> listAuthorInfoById(Collection<Integer> userIds, UserOptionsBuilder builder) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        List<User> users = RedisClient.getMultiCacheObject(RedisConstants.USER_INFO, userIds, nullIds -> {
            List<User> absentUsers = userRepository.getUserByIds(nullIds);
            Map<String, User> map = ListMapperHandler.listToMap(absentUsers, user -> RedisConstants.USER_INFO + user.getUserId());
            RedisClient.cacheMultiObject(map, 10, 20, TimeUnit.DAYS);
            return absentUsers;
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
        return getAuthorInfoById(userId, null);
    }

    @Override
    public UserDTO getAuthorInfoById(int userId, UserOptionsBuilder builder) {
        User user = this.getUserById(userId);
        UserDTO dto = UserConverter.INSTANCE.toUserDTO(user);
        if (builder == null || builder.stat) {
            UserStat stat = countService.getUserStatById(userId);
            dto.setStat(stat);
        }
        if (builder == null || builder.follow) {
            buildFollowState(dto);
        }
        return dto;
    }

    @Override
    public String updateAvatar(MultipartFile avatar) {
        //更新头像, 登录后才能上传
        int loginUserId = LoginUserContext.getLoginUserId();
        String url = imageService.upload(avatar, "avatar", th -> th.size(120, 120));
        boolean success = userRepository.updateAvatar(loginUserId, url);
        if (success) {
            UserRabbitMessage message = new UserRabbitMessage(UserRabbitMessage.Type.USER_UPDATE, loginUserId);
            rabbitMessagePublisher.publishMsg(message);
        }
        return url;
    }

    @Override
    public boolean updateProfile(User user) {
        int loginUserId = LoginUserContext.getLoginUserId();

        Boolean success = transactionTemplate.execute(status -> {
            //暂不支持修改
            //更新账号信息
//            if (saved && StringUtils.hasText(username)) {
//                Account account = new Account();
//                account.setId(loginUserId);
//                account.setUsername(username);
//                return accountRepository.updateById(account);
//            }
            return userRepository.updateUser(loginUserId, user);
        });
        if (Boolean.TRUE.equals(success)) {
            UserRabbitMessage message = new UserRabbitMessage(UserRabbitMessage.Type.USER_UPDATE, loginUserId);
            rabbitMessagePublisher.publishMsg(message);
            return true;
        }
        return false;
    }

    private void buildUserStat(List<UserDTO> userDTOS) {
        //用户数据
        List<Integer> userIds = ListMapperHandler.listTo(userDTOS, UserDTO::getUserId);
        Map<Integer, UserStat> stats = countService.getUserStatByUserIds(userIds);
        ListMapperHandler.doAssemble(userDTOS, UserDTO::getUserId, stats, UserDTO::setStat);
    }

    private void buildFollowState(List<UserDTO> userDTOS) {
        Integer loginUserId = LoginUserContext.getLoginUserIdDefaultNull();
        if (loginUserId == null) {
            return;
        }
        //待判定的用户
        List<Integer> followings = ListMapperHandler.listTo(userDTOS, UserDTO::getUserId);
        Map<Integer, Boolean> followed = followService.hasFollowed(loginUserId, followings);
        if (followed.isEmpty()) return;
        ListMapperHandler.doAssemble(userDTOS, UserDTO::getUserId, followed,
                UserDTO::setFollowed);
    }

    private void buildFollowState(UserDTO userDTO) {
        Integer loginUserId = LoginUserContext.getLoginUserIdDefaultNull();
        if (loginUserId == null) return;
        userDTO.setFollowed(followService.hasFollowed(loginUserId, userDTO.getUserId()));
    }

    private void buildLoginUserStat(LoginProfile loginProfile, int loginUserId) {
        //用户数据
        UserStat stat = countService.getUserStatById(loginUserId);
        loginProfile.setStat(stat);
        //获取登录用户点赞的文章数
        Integer likes = likeService.getUserLikeCount(loginUserId, LikeType.ARTICLE).intValue();
        loginProfile.setLikes(likes);
        //获取登录用户收藏的文章数
        Integer collects = collectService.getUserCollectCount(loginUserId).intValue();
        loginProfile.setCollects(collects);
        //获取登录用户关注的用户数
        Integer followings = followService.getUserFollowingCount(loginUserId).intValue();
        Integer followers = followService.getUserFollowerCount(loginUserId).intValue();
        loginProfile.setFollowers(followers);
        loginProfile.setFollowings(followings);
    }

    private User loadUserCache(String redisKey, Integer userId) {
        User user;
        synchronized (this) {
            user = RedisClient.getCacheObject(redisKey);
            if (user == null) {
                user = userRepository.getUserById(userId);
                if (user == null) {
                    RedisClient.cacheEmptyObject(redisKey, new User());
                } else {
                    RedisClient.setCacheObject(redisKey, user, RandomUtils.randomLong(10, 20), TimeUnit.DAYS);
                }
            }
            return user;
        }
    }

}
