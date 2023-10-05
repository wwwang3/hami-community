package top.wang3.hami.core.service.interact.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;
import top.wang3.hami.common.dto.FollowCountItem;
import top.wang3.hami.common.model.UserFollow;

import java.util.List;
import java.util.Map;

public interface FollowRepository extends IService<UserFollow> {

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

    List<FollowCountItem> listUserFollowingCount(List<Integer> userIds);

    List<FollowCountItem> listUserFollowerCount(List<Integer> userIds);

    List<UserFollow> listUserFollowings(Integer userId);

    List<UserFollow> listUserFollowers(Integer userId);

    List<Integer> listUserFollowings(Page<UserFollow> page, int userId);

    List<Integer> listUserFollowers(Page<UserFollow> page, int userId);

    @Transactional(rollbackFor = Exception.class)
    boolean follow(int userId, int followingId);

    @Transactional(rollbackFor = Exception.class)
    boolean unFollow(int userId, int followingId);

}