package top.wang3.hami.core.service.user.impl;

import cn.xuyanwu.spring.file.storage.FileInfo;
import cn.xuyanwu.spring.file.storage.FileStorageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.converter.UserConverter;
import top.wang3.hami.common.dto.LoginProfile;
import top.wang3.hami.common.dto.UserProfile;
import top.wang3.hami.common.model.Account;
import top.wang3.hami.common.model.User;
import top.wang3.hami.core.mapper.AccountMapper;
import top.wang3.hami.core.mapper.UserMapper;
import top.wang3.hami.core.service.article.ArticleCollectService;
import top.wang3.hami.core.service.like.LikeService;
import top.wang3.hami.core.service.user.UserFollowService;
import top.wang3.hami.core.service.user.UserService;
import top.wang3.hami.security.context.LoginUserContext;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    private final LikeService likeService;
    private final ArticleCollectService articleCollectService;
    private final UserFollowService userFollowService;
    private final FileStorageService fileStorageService;

    private final AccountMapper accountMapper;

    @Resource
    TransactionTemplate transactionTemplate;

    public UserServiceImpl(LikeService likeService, ArticleCollectService articleCollectService,
                           UserFollowService userFollowService, FileStorageService fileStorageService,
                           AccountMapper accountMapper) {
        this.likeService = likeService;
        this.articleCollectService = articleCollectService;
        this.userFollowService = userFollowService;
        this.fileStorageService = fileStorageService;
        this.accountMapper = accountMapper;
    }

    @Override
    public LoginProfile getLoginProfile() {
        int loginUserId = LoginUserContext.getLoginUserId();
        User user = ChainWrappers
                .queryChain(getBaseMapper())
                .select("user_id", "username", "avatar", "profile", "tag", "ctime")
                .eq("user_id", loginUserId)
                .one();
        final LoginProfile loginProfile = UserConverter.INSTANCE.toLoginProfile(user);
        //获取登录用户点赞的文章数
        Long likes = likeService.getUserLikes(loginUserId, Constants.LIKE_TYPE_ARTICLE);
        loginProfile.setLikes(likes);
        //获取登录用户收藏的文章数
        Long collects = articleCollectService.getUserCollects(loginUserId);
        loginProfile.setCollects(collects);
        //获取登录用户关注的用户数
        Long followings = userFollowService.getUserFollowings(loginUserId);
        Long followers = userFollowService.getUserFollowers(loginUserId);
        loginProfile.setFollowers(followers);
        loginProfile.setFollowings(followings);
        return loginProfile;
    }

    @Override
    public UserProfile getUserProfile() {
        int userId = LoginUserContext.getLoginUserId();
        User user = ChainWrappers.queryChain(getBaseMapper())
                .eq("user_id", userId)
                .one();
        return UserConverter.INSTANCE.toUserProfile(user);
    }

    @Override
    public String updateAvatar(MultipartFile avatar) {
        //更新头像, 登录后才能上传
        int loginUserId = LoginUserContext.getLoginUserId();
        FileInfo info = fileStorageService.of(avatar)
                .image(img -> img.size(120, 120))
                .setObjectId(loginUserId)
                .setObjectType("avatar")
                .upload();
        String url = info.getUrl();
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
}
