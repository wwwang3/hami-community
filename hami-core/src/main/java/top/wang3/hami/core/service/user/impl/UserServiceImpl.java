package top.wang3.hami.core.service.user.impl;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.converter.UserConverter;
import top.wang3.hami.common.dto.builder.UserOptionsBuilder;
import top.wang3.hami.common.dto.user.LoginProfile;
import top.wang3.hami.common.dto.user.UserDTO;
import top.wang3.hami.common.dto.user.UserProfile;
import top.wang3.hami.common.dto.user.UserStat;
import top.wang3.hami.common.message.UserRabbitMessage;
import top.wang3.hami.common.model.User;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.annotation.CostLog;
import top.wang3.hami.core.component.RabbitMessagePublisher;
import top.wang3.hami.core.repository.AccountRepository;
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
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Resource
    ImageService imageService;

    private final UserRepository userRepository;
    private final UserInteractService userInteractService;
    private final CountService countService;
    private final AccountRepository accountRepository;
    private final RabbitMessagePublisher rabbitMessagePublisher;

    @Resource
    TransactionTemplate transactionTemplate;

    @Override
    public LoginProfile getLoginProfile() {
        int loginUserId = LoginUserContext.getLoginUserId();
        User user = this.getUserById(loginUserId);
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
        User user = this.getUserById(userId);
        return UserConverter.INSTANCE.toUserProfile(user);
    }

    @Override
    public User getUserById(Integer userId) {
        if (userId == null || userId < 0) {
            throw new IllegalArgumentException("参数异常");
        }
        String redisKey = Constants.USER_INFO + userId;
        User user = RedisClient.getCacheObject(redisKey);
        if (user == null) {
            //todo 保证只有一个请求查询数据库写入Redis
            user = userRepository.getUserById(userId);
            if (user == null) {
                RedisClient.setCacheObject(redisKey,  new User(), 10, TimeUnit.SECONDS);
            } else{
                RedisClient.setCacheObject(redisKey, user);
            }
        } else if (user.getUserId() == null) {
            return null;
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
        List<User> users = RedisClient.getMultiCacheObject(keys, (nullIndexes) -> {
            List<Integer> nullIds = ListMapperHandler.listTo(nullIndexes, userIds::get, false);
            List<User> nullUsers = userRepository.getUserByIds(nullIds);
            Map<String, User> map = ListMapperHandler.listToMap(nullUsers,
                    u -> Constants.USER_INFO + u.getUserId());
            RedisClient.cacheMultiObject(map);
            return nullUsers;
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
        }
        return false;
    }


    private void buildUserStat(List<UserDTO> userDTOS) {
        //用户数据
        List<Integer> userIds = ListMapperHandler.listTo(userDTOS, UserDTO::getUserId);
        List<UserStat> stats = countService.getUserStatByUserIds(userIds);
        ListMapperHandler.doAssemble(userDTOS, UserDTO::getUserId, stats, UserStat::getUserId, UserDTO::setStat);
    }

    private void buildFollowState(List<UserDTO> userDTOS) {
        Integer loginUserId = LoginUserContext.getLoginUserIdDefaultNull();
        if (loginUserId == null) {
            return;
        }
        //待判定的用户
        List<Integer> followings = ListMapperHandler.listTo(userDTOS, UserDTO::getUserId);
        Map<Integer, Boolean> followed = userInteractService.hasFollowed(loginUserId, followings);
        if (followed.isEmpty()) return;
        ListMapperHandler.doAssemble(userDTOS, UserDTO::getUserId, followed,
                UserDTO::setFollowed);
    }

    private void buildFollowState(UserDTO userDTO) {
        Integer loginUserId = LoginUserContext.getLoginUserIdDefaultNull();
        if (loginUserId == null) return;
        userDTO.setFollowed(userInteractService.hasFollowed(loginUserId, userDTO.getUserId()));
    }

}
