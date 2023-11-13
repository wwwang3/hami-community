package top.wang3.hami.core.service.user.repository;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import top.wang3.hami.common.model.User;
import top.wang3.hami.core.mapper.UserMapper;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl extends ServiceImpl<UserMapper, User>
        implements UserRepository {

    private static final String[] LOGIN_PROFILE_FIELDS = {"user_id", "username", "avatar", "profile", "tag", "ctime"};
    public static final String[] USER_PROFILE_FIELDS = {
            "user_id", "username", "avatar", "profile",
            "blog", "company", "position", "tag", "ctime", "mtime"
    };

    @Override
    public User getUserById(Integer userId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select(USER_PROFILE_FIELDS)
                .eq("user_id", userId)
                .one();
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
    public boolean updateUser(Integer loginUserId, final User user) {
        //更新用户信息
        Assert.notNull(user, "user cannot be null");
        user.setUserId(loginUserId);
        return super.updateById(user);
    }

    @Override
    public boolean checkUserExist(Integer userId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("user_id")
                .eq("user_id", userId)
                .exists();
    }
}
