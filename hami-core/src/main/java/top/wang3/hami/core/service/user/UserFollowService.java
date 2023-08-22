package top.wang3.hami.core.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.model.UserFollow;


public interface UserFollowService extends IService<UserFollow> {

    /**
     * 获取用户关注数
     * @param userId 用户ID
     * @return 关注数
     */
    Long getUserFollowings(Integer userId);

    /**
     * 获取用户粉丝数
     * @param userId 用户ID
     * @return 粉丝数
     */
    Long getUserFollowers(Integer userId);
}
