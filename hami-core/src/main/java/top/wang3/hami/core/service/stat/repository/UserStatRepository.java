package top.wang3.hami.core.service.stat.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import top.wang3.hami.common.model.UserStat;

import java.util.List;

public interface UserStatRepository extends IService<UserStat> {

    UserStat selectUserStatById(Integer userId);

    List<UserStat> selectUserStatByIds(List<Integer> userIds);

    boolean updateArticles(Integer userId, int delta);

    boolean updateFollowings(Integer userId, int delta);

    boolean updateFollowers(Integer userId, int delta);

    boolean updateViews(Integer userId, int delta);

    boolean updateLikes(Integer userId, int delta);

    boolean updateComments(Integer userId, int delta);

    boolean updateCollects(Integer userId, int delta);
}
