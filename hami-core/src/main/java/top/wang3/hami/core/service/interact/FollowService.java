package top.wang3.hami.core.service.interact;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.lang.NonNull;
import top.wang3.hami.common.model.UserFollow;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public interface FollowService {

    boolean follow(int followingId);

    boolean unFollow(int followingId);

    /**
     * 获取用户关注数
     * @param userId 用户ID
     * @return 用户关注数
     */
    @NonNull
    Long getUserFollowingCount(Integer userId);

    /**
     * 获取用户粉丝数
     * @param userId 用户ID
     * @return 用户粉丝数
     */
    @NonNull
    Long getUserFollowerCount(Integer userId);

    boolean hasFollowed(Integer userId, Integer followingId);

    Map<Integer, Boolean> hasFollowed(Integer userId, List<Integer> followingIds);

    Map<Integer, Long> listUserFollowingCount(List<Integer> userIds);

    Map<Integer, Long> listUserFollowerCount(List<Integer> userIds);

    List<Integer> listUserFollowings(Page<UserFollow> page, int userId);

    List<Integer> listUserFollowers(Page<UserFollow> page, int userId);

    List<Integer> loadUserFollowings(Integer userId);

    List<Integer> loadUserFollowers(Integer userId);
}
