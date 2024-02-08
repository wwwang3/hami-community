package top.wang3.hami.core.service.interact;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.lang.NonNull;
import top.wang3.hami.common.model.UserFollow;

import java.util.List;
import java.util.Map;

public interface FollowService {

    boolean follow(int followingId);

    boolean unFollow(int followingId);

    /**
     * 获取用户关注数
     *
     * @param userId 用户ID
     * @return 用户关注数
     */
    @NonNull
    Integer getUserFollowingCount(Integer userId);

    /**
     * 获取用户粉丝数
     *
     * @param userId 用户ID
     * @return 用户粉丝数
     */
    @NonNull
    @SuppressWarnings("UnusedReturnValue")
    Integer getUserFollowerCount(Integer userId);

    boolean hasFollowed(Integer userId, Integer followingId);

    Map<Integer, Boolean> hasFollowed(Integer userId, List<Integer> followingIds);

    List<Integer> listUserFollowings(Page<UserFollow> page, int userId);

    List<Integer> listUserFollowers(Page<UserFollow> page, int userId);

    @SuppressWarnings("UnusedReturnValue")
    List<Integer> loadUserFollowings(Integer userId);

    @SuppressWarnings("UnusedReturnValue")
    List<Integer> loadUserFollowers(Integer userId);
}
