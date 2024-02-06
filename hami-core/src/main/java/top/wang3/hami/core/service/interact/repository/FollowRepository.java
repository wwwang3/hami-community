package top.wang3.hami.core.service.interact.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.model.UserFollow;

import java.util.List;
import java.util.Map;

public interface FollowRepository extends IService<UserFollow> {

    /**
     * 获取用户关注数
     *
     * @param userId 用户ID
     * @return 用户关注数
     */
    Integer getUserFollowingCount(Integer userId);

    /**
     * 获取用户粉丝数
     *
     * @param userId 用户ID
     * @return 用户粉丝数
     */
    Integer getUserFollowerCount(Integer userId);

    boolean hasFollowed(Integer userId, Integer followingId);

    Map<Integer, Boolean> hasFollowed(Integer userId, List<Integer> followingIds);

    Map<Integer, Long> listUserFollowingCount(List<Integer> userIds);

    Map<Integer, Long> listUserFollowerCount(List<Integer> userIds);

    List<UserFollow> listUserFollowings(Integer userId);

    List<UserFollow> listUserFollowers(Integer userId);

    List<Integer> listUserFollowings(Page<UserFollow> page, int userId);

    List<Integer> listUserFollowers(Page<UserFollow> page, int userId);

    boolean follow(int userId, int followingId);

    boolean unFollow(int userId, int followingId);

    boolean followUser(int userId, int toUserId, byte state);


}
