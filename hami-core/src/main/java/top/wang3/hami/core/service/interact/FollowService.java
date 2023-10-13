package top.wang3.hami.core.service.interact;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import top.wang3.hami.common.model.UserFollow;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface FollowService {

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

    Collection<Integer> listUserFollowings(Page<UserFollow> page, int userId);

    Collection<Integer> listUserFollowers(Page<UserFollow> page, int userId);

    @Transactional(rollbackFor = Exception.class)
    boolean follow(int followingId);

    @Transactional(rollbackFor = Exception.class)
    boolean unFollow(int followingId);

    List<Integer> loadUserFollowings(String key, Integer userId, long current, long size);

    List<Integer> loadUserFollowers(String key, Integer userId, long current, long size);
}
