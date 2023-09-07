package top.wang3.hami.core.service.user.impl;

import cn.xuyanwu.spring.file.storage.FileStorageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.converter.UserConverter;
import top.wang3.hami.common.dto.LoginProfile;
import top.wang3.hami.common.dto.SimpleUserDTO;
import top.wang3.hami.common.dto.UserProfile;
import top.wang3.hami.common.dto.UserStat;
import top.wang3.hami.common.model.Account;
import top.wang3.hami.common.model.User;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.core.mapper.AccountMapper;
import top.wang3.hami.core.mapper.UserMapper;
import top.wang3.hami.core.service.article.ArticleCollectService;
import top.wang3.hami.core.service.common.CountService;
import top.wang3.hami.core.service.common.ImageService;
import top.wang3.hami.core.service.common.UserInteractService;
import top.wang3.hami.core.service.like.LikeService;
import top.wang3.hami.core.service.user.UserFollowService;
import top.wang3.hami.core.service.user.UserService;
import top.wang3.hami.security.context.LoginUserContext;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    private static final String[] LOGIN_PROFILE_FIELDS = {"user_id", "username", "avatar", "profile", "tag", "ctime"};
    public static final String[] USER_PROFILE_FIELDS = {
            "user_id", "username", "avatar", "profile",
            "blog", "company", "position", "tag", "ctime"
    };
    @Resource
    ImageService imageService;
    @Resource
    TransactionTemplate transactionTemplate;

    private final LikeService likeService;
    private final ArticleCollectService articleCollectService;
    private final UserFollowService userFollowService;
    private final FileStorageService fileStorageService;
    private final AccountMapper accountMapper;
    private final UserInteractService userInteractService;
    private final CountService countService;

    @Override
    public LoginProfile getLoginProfile() {
        int loginUserId = LoginUserContext.getLoginUserId();
        User user = ChainWrappers
                .queryChain(getBaseMapper())
                .select(LOGIN_PROFILE_FIELDS)
                .eq("user_id", loginUserId)
                .one();
        final LoginProfile loginProfile = UserConverter.INSTANCE.toLoginProfile(user);
        //获取登录用户点赞的文章数
        Long likes = likeService.getUserLikeCount(loginUserId, Constants.LIKE_TYPE_ARTICLE);
        loginProfile.setLikes(likes);
        //获取登录用户收藏的文章数
        Long collects = articleCollectService.getUserCollects(loginUserId);
        loginProfile.setCollects(collects);
        //获取登录用户关注的用户数
        Long followings = userFollowService.getUserFollowingCount(loginUserId);
        Long followers = userFollowService.getUserFollowerCount(loginUserId);
        loginProfile.setFollowers(followers);
        loginProfile.setFollowings(followings);
        return loginProfile;
    }

    @Override
    public UserProfile getUserProfile() {
        int userId = LoginUserContext.getLoginUserId();
        User user = ChainWrappers.queryChain(getBaseMapper())
                .select(USER_PROFILE_FIELDS)
                .eq("user_id", userId)
                .one();
        return UserConverter.INSTANCE.toUserProfile(user);
    }

    @Override
    public String updateAvatar(MultipartFile avatar) {
        //更新头像, 登录后才能上传
        int loginUserId = LoginUserContext.getLoginUserId();
        String url = imageService.upload(avatar, "avatar", th -> th.size(120, 120));
        //更新头像地址
        transactionTemplate.execute(status ->
                ChainWrappers.updateChain(getBaseMapper())
                .set("avatar", url)
                .eq("user_id", loginUserId)
                .update());
        return url;
    }

    @Override
    public boolean updateProfile(User user) {
        int loginUserId = LoginUserContext.getLoginUserId();
        //更新用户信息
        user.setUserId(loginUserId);
        boolean saved = super.updateById(user);
        String username = user.getUsername();
        //更新账号信息
        if (StringUtils.hasText(username)) {
            Account account = new Account();
            account.setId(loginUserId);
            account.setUsername(username);
            accountMapper.updateById(account);
        }
        return saved;
    }

    @Override
    public List<SimpleUserDTO> getAuthorInfoByIds(List<Integer> userIds) {
        List<User> user = ChainWrappers.queryChain(getBaseMapper())
                .select(USER_PROFILE_FIELDS)
                .in("user_id", userIds)
                .list();
        List<SimpleUserDTO> dtos = UserConverter.INSTANCE.toUserDTOList(user);
        //查询用户的粉丝数据
        //查询关注状态
        buildUserStat(dtos);
        buildFollowState(dtos);
        return dtos;
    }

    private void buildUserStat(List<SimpleUserDTO> userDTOS) {
        //用户数据
        userDTOS.forEach(dto -> {
            UserStat stat = countService.getUserStatById(dto.getUserId());
            dto.setFollowers(stat.getFollowers());
            dto.setFollowings(stat.getFollowings());
        });
    }

    private void buildFollowState(List<SimpleUserDTO> userDTOS) {
        LoginUserContext.getOptLoginUserId()
                .ifPresent(id -> {
                    //待判定的用户
                    List<Integer> followings = ListMapperHandler.listTo(userDTOS, SimpleUserDTO::getUserId);
                    Map<Integer, Boolean> followed = userInteractService.hasFollowed(id, followings);
                    if (followed.isEmpty()) return;
                    ListMapperHandler.doAssemble(userDTOS, SimpleUserDTO::getUserId, followed,
                            SimpleUserDTO::setFollowed);
                });
    }
}
