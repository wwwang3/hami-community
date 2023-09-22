package top.wang3.hami.core.service.user.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.FollowCountItem;
import top.wang3.hami.common.model.UserFollow;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.core.exception.ServiceException;
import top.wang3.hami.core.mapper.UserFollowMapper;
import top.wang3.hami.core.service.user.UserFollowService;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow>
        implements UserFollowService {

    @Override
    public Integer getUserFollowingCount(Integer userId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("user_id")
                .eq("user_id", userId)
                .eq("`state`", Constants.ONE)
                .count().intValue();
    }

    @Override
    public Integer getUserFollowerCount(Integer userId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("following")
                .eq("following", userId)
                .eq("`state`", Constants.ONE)
                .count().intValue();
    }

    @Override
    public List<FollowCountItem> getUserFollowingCount(List<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) return Collections.emptyList();
        return getBaseMapper().selectUserFollowingCount(userIds);
    }

    @Override
    public List<FollowCountItem> getUserFollowerCount(List<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        return getBaseMapper().selectUserFollowerCount(userIds);
    }


    @Override
    public List<Integer> getUserFollowings(Page<UserFollow> page, int userId) {
        List<UserFollow> followings = ChainWrappers.queryChain(getBaseMapper())
                .select("user_id", "followings", "ctime", "mtime")
                .eq("user_id", userId)
                .eq("`state`", Constants.ONE)
                .list(page);
        return ListMapperHandler.listTo(followings, UserFollow::getFollowing);
    }

    @Override
    public List<Integer> getUserFollowers(Page<UserFollow> page,int userId) {
        List<UserFollow> followers = ChainWrappers.queryChain(getBaseMapper())
                .select("user_id", "followings", "ctime", "mtime")
                .eq("following", userId)
                .eq("`state`", Constants.ONE)
                .orderByDesc("mtime")
                .list(page);
        return ListMapperHandler.listTo(followers, UserFollow::getUserId);
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
            follow.setState(Constants.ONE);
            return super.save(follow);
        } else if (Objects.equals(userFollow.getState(), Constants.ONE)) {
            throw new ServiceException("重复关注");
        } else if (Objects.equals(userFollow.getState(), Constants.ZERO)) {
            //修改关注状态
            return ChainWrappers.updateChain(getBaseMapper())
                    .set("`state`", Constants.ONE)
                    .eq("user_id", userId)
                    .eq("following", followingId)
                    .eq("`state`", Constants.ZERO)
                    .update();
        }
        return false;
    }

    @Transactional
    @Override
    public boolean unFollow(int userId, int followingId) {
        return ChainWrappers.updateChain(getBaseMapper())
                .set("`state`", Constants.ZERO)
                .eq("user_id", userId)
                .eq("following", followingId)
                .eq("`state`", Constants.ONE)
                .update();
    }

    @Override
    public List<UserFollow> getUserFollowings(Integer userId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("user_id", "following", "mtime")
                .eq("user_id", userId)
                .eq("`state`", Constants.ZERO)
                .orderByDesc("mtime")
                .last("limit 2000")
                .list();
    }

    @Override
    public List<UserFollow> getUserFollowers(Integer userId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("user_id", "following", "mtime")
                .eq("followings", userId)
                .eq("`state`", Constants.ZERO)
                .orderByDesc("mtime")
                .last("limit 1000")
                .list();
    }
}
