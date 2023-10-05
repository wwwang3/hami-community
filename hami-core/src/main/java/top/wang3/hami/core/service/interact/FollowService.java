package top.wang3.hami.core.service.interact;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.transaction.annotation.Transactional;
import top.wang3.hami.common.model.UserFollow;

import java.util.List;
import java.util.Map;

public interface FollowService {

    /**
     * 获取用户关注数
     * @param userId 用户ID
     * @return 用户关注数
     */
    Long getUserFollowingCount(Integer userId);

    /**
     * 获取用户粉丝数
     * @param userId 用户ID
     * @return 用户粉丝数
     */
    Long getUserFollowerCount(Integer userId);

    boolean hasFollowed(Integer userId, Integer followingId);

    Map<Integer, Boolean> hasFollowed(Integer userId, List<Integer> followingIds);

    Map<Integer, Long> listUserFollowingCount(List<Integer> userIds);

    Map<Integer, Long> listUserFollowerCount(List<Integer> userIds);

    List<UserFollow> listUserFollowings(Integer userId);

    List<UserFollow> listUserFollowers(Integer userId);

    List<Integer> listUserFollowings(Page<UserFollow> page, int userId);

    List<Integer> listUserFollowers(Page<UserFollow> page, int userId);

    @Transactional(rollbackFor = Exception.class)
    boolean follow(int followingId);

    @Transactional(rollbackFor = Exception.class)
    boolean unFollow(int followingId);

}