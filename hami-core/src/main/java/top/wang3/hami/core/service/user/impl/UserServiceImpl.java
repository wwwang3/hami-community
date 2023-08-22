package top.wang3.hami.core.service.user.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.converter.UserConverter;
import top.wang3.hami.common.dto.LoginProfile;
import top.wang3.hami.common.model.User;
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

    public UserServiceImpl(LikeService likeService, ArticleCollectService articleCollectService,
                           UserFollowService userFollowService) {
        this.likeService = likeService;
        this.articleCollectService = articleCollectService;
        this.userFollowService = userFollowService;
    }

    @Override
    public LoginProfile getLoginProfile() {
        int loginUserId = LoginUserContext.getLoginUserId();
        User user = ChainWrappers
                .queryChain(getBaseMapper())
                .select("user_id", "username", "avatar", "profile", "tag")
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
}
