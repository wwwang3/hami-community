package top.wang3.hami.core.service.user.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.common.converter.UserConverter;
import top.wang3.hami.common.dto.builder.UserOptionsBuilder;
import top.wang3.hami.common.dto.interact.LikeType;
import top.wang3.hami.common.dto.stat.UserStatDTO;
import top.wang3.hami.common.model.User;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.vo.user.LoginProfile;
import top.wang3.hami.common.vo.user.UserVo;
import top.wang3.hami.core.annotation.CostLog;
import top.wang3.hami.core.service.interact.CollectService;
import top.wang3.hami.core.service.interact.FollowService;
import top.wang3.hami.core.service.interact.LikeService;
import top.wang3.hami.core.service.stat.CountService;
import top.wang3.hami.core.service.user.UserService;
import top.wang3.hami.core.service.user.cache.UserCacheService;
import top.wang3.hami.security.context.LoginUserContext;
import top.wang3.hami.security.model.LoginUser;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final FollowService followService;
    private final CountService countService;
    private final LikeService likeService;
    private final CollectService collectService;
    private final UserCacheService userCacheService;

    @Override
    public LoginProfile getLoginProfile() {
        LoginUser loginUser = LoginUserContext.getLoginUser();
        User user = userCacheService.getUserCache(loginUser.getId());
        LoginProfile loginProfile = UserConverter.INSTANCE.toLoginProfile(user, loginUser.getUsername());
        loginProfile.setAccount(loginUser.getUsername());
        buildLoginUserStat(loginProfile, loginUser.getId());
        return loginProfile;
    }

    @Override
    public UserVo getAuthorInfoById(int userId) {
        return getAuthorInfoById(userId, null);
    }

    @Override
    public UserVo getAuthorInfoById(int userId, UserOptionsBuilder builder) {
        User user = userCacheService.getUserCache(userId);
        if (user == null) {
            return null;
        }
        UserVo vo = UserConverter.INSTANCE.toUserVo(user);
        if (builder == null || builder.stat) {
            UserStatDTO userStat = countService.getUserStatDTOById(userId);
            vo.setStat(userStat);
        }
        if (builder == null || builder.follow) {
            buildFollowState(vo);
        }
        return vo;
    }

    @CostLog
    @Override
    public List<UserVo> listAuthorById(List<Integer> userIds, UserOptionsBuilder builder) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        List<User> users = userCacheService.listUserById(userIds);
        List<UserVo> vos = UserConverter.INSTANCE.toUserVoList(users);
        if (builder == null || builder.stat) {
            //查询用户的粉丝数据
            buildUserStat(userIds, vos);
        }
        //查询关注状态
        if (builder == null || builder.follow) {
            buildFollowState(userIds, vos);
        }
        return vos;
    }

    private void buildUserStat(List<Integer> userIds, List<UserVo> userDTOS) {
        // 用户数据
        Map<Integer, UserStatDTO> stats = countService.getUserStatByIds(userIds);
        ListMapperHandler.doAssemble(userDTOS, UserVo::getUserId, stats, UserVo::setStat);
    }

    private void buildFollowState(List<Integer> userIds, List<UserVo> userDTOS) {
        // 关注状态
        Integer loginUserId = LoginUserContext.getLoginUserIdDefaultNull();
        if (loginUserId == null) {
            return;
        }
        Map<Integer, Boolean> followed = followService.hasFollowed(loginUserId, userIds);
        if (followed.isEmpty()) return;
        ListMapperHandler.doAssemble(
                userDTOS,
                UserVo::getUserId,
                followed,
                UserVo::setFollowed
        );
    }

    private void buildFollowState(UserVo userDTO) {
        Integer loginUserId = LoginUserContext.getLoginUserIdDefaultNull();
        if (loginUserId == null) return;
        userDTO.setFollowed(followService.hasFollowed(loginUserId, userDTO.getUserId()));
    }

    private void buildLoginUserStat(LoginProfile loginProfile, int loginUserId) {
        // 用户数据
        UserStatDTO stat = countService.getUserStatDTOById(loginUserId);
        loginProfile.setStat(stat);
        // 获取登录用户点赞的文章数
        Integer likes = likeService.getUserLikeCount(loginUserId, LikeType.ARTICLE);
        loginProfile.setLikes(likes);
        // 获取登录用户收藏的文章数
        Integer collects = collectService.getUserCollectCount(loginUserId);
        loginProfile.setCollects(collects);
        // 获取登录用户关注的用户数和粉丝数
        loginProfile.setFollowers(stat.getTotalFollowers());
        loginProfile.setFollowings(stat.getTotalFollowings());
    }

}
