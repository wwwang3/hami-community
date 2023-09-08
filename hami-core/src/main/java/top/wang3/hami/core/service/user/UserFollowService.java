package top.wang3.hami.core.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.model.UserFollow;

import java.util.List;


public interface UserFollowService extends IService<UserFollow> {

    /**
     * 获取用户关注数
     * @param userId 用户ID
     * @return 关注数
     */
    Integer getUserFollowingCount(Integer userId);

    /**
     * 获取用户粉丝数
     * @param userId 用户ID
     * @return 粉丝数
     */
    Integer getUserFollowerCount(Integer userId);

    List<Integer> getUserFollowings(Page<UserFollow> page, int userId);

    List<Integer> getUserFollowers(Page<UserFollow> page, int userId);

    boolean follow(int userId, int followingId);

    boolean unFollow(int userId, int followingId);

}
