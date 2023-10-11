package top.wang3.hami.core.service.interact.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Repository;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.FollowCountItem;
import top.wang3.hami.common.model.UserFollow;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.core.component.ZPageHandler;
import top.wang3.hami.core.exception.ServiceException;
import top.wang3.hami.core.mapper.UserFollowMapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public class FollowRepositoryImpl extends ServiceImpl<UserFollowMapper, UserFollow>
        implements FollowRepository {

    @Override
    public Long getUserFollowingCount(Integer userId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .eq("user_id", userId)
                .eq("`state`", Constants.ONE)
                .count();
    }

    @Override
    public Long getUserFollowerCount(Integer userId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .eq("following", userId)
                .eq("`state`", Constants.ONE)
                .count();
    }

    @Override
    public boolean hasFollowed(Integer userId, Integer followingId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .eq("user_id", userId)
                .eq("following", followingId)
                .eq("`state`", Constants.ONE)
                .exists();
    }

    @Override
    public Map<Integer, Boolean> hasFollowed(Integer userId, List<Integer> followingIds) {
        List<UserFollow> items = ChainWrappers.queryChain(getBaseMapper())
                .select("following", "`state`")
                .eq("user_id", userId)
                .eq("`state`", Constants.ONE)
                .in("following", followingIds)
                .list();
        return ListMapperHandler.listToMap(items, UserFollow::getFollowing, i -> Boolean.TRUE);
    }

    @Override
    public Map<Integer, Long> listUserFollowingCount(List<Integer> userIds) {
        List<FollowCountItem> countItems = getBaseMapper().selectUserFollowingCount(userIds);
        return ListMapperHandler.listToMap(countItems, FollowCountItem::getUserId, FollowCountItem::getCount);
    }

    @Override
    public Map<Integer, Long> listUserFollowerCount(List<Integer> userIds) {
        List<FollowCountItem> countItems = getBaseMapper().selectUserFollowerCount(userIds);
        return ListMapperHandler.listToMap(countItems, FollowCountItem::getUserId, FollowCountItem::getCount);
    }

    @Override
    public List<UserFollow> listUserFollowings(Integer userId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("following", "mtime")
                .eq("user_id", userId)
                .eq("`state`", Constants.ONE)
                .orderByDesc("mtime")
                .last("limit " + ZPageHandler.DEFAULT_MAX_SIZE)
                .list();
    }

    @Override
    public List<UserFollow> listUserFollowers(Integer userId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select("user_id", "mtime")
                .eq("following", userId)
                .eq("`state`", Constants.ONE)
                .orderByDesc("mtime")
                .last("limit " + ZPageHandler.DEFAULT_MAX_SIZE)
                .list();
    }

    @Override
    public List<Integer> listUserFollowings(Page<UserFollow> page, int userId) {
        //获取用户关注 回源DB方法
        List<UserFollow> followings = ChainWrappers.queryChain(getBaseMapper())
                .select("following")
                .eq("user_id", userId)
                .eq("`state`", Constants.ONE)
                .list(page);
        return ListMapperHandler.listTo(followings, UserFollow::getFollowing);
    }

    @Override
    public List<Integer> listUserFollowers(Page<UserFollow> page, int userId) {
        //获取用户粉丝 回源DB方法
        List<UserFollow> followings = ChainWrappers.queryChain(getBaseMapper())
                .select("user_id")
                .eq("following", userId)
                .eq("`state`", Constants.ONE)
                .list(page);
        return ListMapperHandler.listTo(followings, UserFollow::getUserId);
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
