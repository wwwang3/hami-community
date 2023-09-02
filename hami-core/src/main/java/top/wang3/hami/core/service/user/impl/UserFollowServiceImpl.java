package top.wang3.hami.core.service.user.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.model.UserFollow;
import top.wang3.hami.core.exception.ServiceException;
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

    @Override
    public boolean follow(int userId, int followingId) {
        //关注
        UserFollow userFollow = ChainWrappers.queryChain(getBaseMapper())
                .eq("user_id", userId)
                .eq("following", followingId)
                .one();
        if (userFollow == null) {
            //不存在这条记录
            UserFollow follow = new UserFollow();
            follow.setUserId(userId);
            follow.setFollowing(followingId);
            follow.setState(1);
            return super.save(follow);
        } else if (userFollow.getState() == 1) {
            throw new ServiceException("重复关注");
        } else if (userFollow.getState() == 0) {
            //修改关注状态
            return ChainWrappers.updateChain(getBaseMapper())
                    .set("`state`", 1)
                    .eq("user_id", userId)
                    .eq("following", followingId)
                    .eq("`state`", 0)
                    .update();
        }
        return false;
    }

    @Override
    public boolean unFollow(int userId, int followingId) {
        return ChainWrappers.updateChain(getBaseMapper())
                .set("`state`", Constants.ZERO)
                .eq("user_id", userId)
                .eq("following", followingId)
                .eq("`state`", Constants.ONE)
                .update();
    }

}
