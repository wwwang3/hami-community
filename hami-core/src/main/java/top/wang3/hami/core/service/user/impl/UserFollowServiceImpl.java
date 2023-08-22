package top.wang3.hami.core.service.user.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.model.UserFollow;
import top.wang3.hami.core.mapper.UserFollowMapper;
import top.wang3.hami.core.service.user.UserFollowService;

@Service
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow>
        implements UserFollowService {
    @Override
    public Long getUserFollowings(Integer userId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("user_id")
                .eq("user_id", userId)
                .eq("`state`", Constants.ONE)
                .count();
    }

    @Override
    public Long getUserFollowers(Integer userId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("following")
                .eq("following", userId)
                .eq("`state`", Constants.ONE)
                .count();
    }
}
