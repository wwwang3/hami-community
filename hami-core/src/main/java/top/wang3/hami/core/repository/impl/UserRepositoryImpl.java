package top.wang3.hami.core.repository.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;
import top.wang3.hami.common.converter.UserConverter;
import top.wang3.hami.common.dto.user.LoginProfile;
import top.wang3.hami.common.dto.user.UserProfile;
import top.wang3.hami.common.model.User;
import top.wang3.hami.core.mapper.UserMapper;
import top.wang3.hami.core.repository.UserRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl extends ServiceImpl<UserMapper, User>
        implements UserRepository {

    private static final String[] LOGIN_PROFILE_FIELDS = {"user_id", "username", "avatar", "profile", "tag", "ctime"};
    public static final String[] USER_PROFILE_FIELDS = {
            "user_id", "username", "avatar", "profile",
            "blog", "company", "position", "tag", "ctime"
    };

    @Resource
    TransactionTemplate transactionTemplate;

    @Override
    public User getUserById(Integer userId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select(USER_PROFILE_FIELDS)
                .eq("user_id", userId)
                .one();
    }

    @Override
    public UserProfile getUserProfile(Integer userId) {
        User user = getUserById(userId);
        return UserConverter.INSTANCE.toUserProfile(user);
    }

    @Override
    public LoginProfile getLoginProfile(Integer userId) {
        User user = ChainWrappers.queryChain(getBaseMapper())
                .select(LOGIN_PROFILE_FIELDS)
                .eq("user_id", userId)
                .one();
        return UserConverter.INSTANCE.toLoginProfile(user);
    }

    @Override
    public List<User> getUserByIds(List<Integer> userIds) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select(USER_PROFILE_FIELDS)
                .in("user_id", userIds)
                .list();
    }

    @Override
    public List<Integer> scanUserIds(int lastUserId, int batchSize) {
        return getBaseMapper().scanUserIds(lastUserId, batchSize);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateAvatar(Integer loginUserId, String url) {
        //更新头像地址
        return ChainWrappers.updateChain(getBaseMapper())
                .set("avatar", url)
                .eq("user_id", loginUserId)
                .update();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateUser(Integer loginUserId, final User user) {
        //更新用户信息
        Assert.notNull(user, "user cannot be null");
        user.setUserId(loginUserId);
        return super.updateById(user);
    }

    @Override
    public boolean checkUserExist(Integer userId) {
        return getBaseMapper().selectById(userId) == null;
    }
}
