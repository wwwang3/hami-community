package top.wang3.hami.core.service.user.repository;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import top.wang3.hami.common.model.User;
import top.wang3.hami.core.mapper.UserMapper;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl extends ServiceImpl<UserMapper, User>
        implements UserRepository {

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
    public List<User> listUserById(List<Integer> userIds) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select(USER_PROFILE_FIELDS)
                .in("user_id", userIds)
                .list();
    }

    @Override
    public List<Integer> scanUserIds(int lastUserId, int batchSize) {
        return getBaseMapper().scanUserIds(lastUserId, batchSize);
    }

    @Override
    public boolean updateUser(Integer loginUserId, final User user) {
        //更新用户信息
        Assert.notNull(user, "user cannot be null");
        user.setUserId(loginUserId);
        return super.updateById(user);
    }
}
