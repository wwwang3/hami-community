package top.wang3.hami.core.service.stat.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import top.wang3.hami.common.model.HotCounter;
import top.wang3.hami.common.model.UserStat;

import java.util.List;


@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface UserStatRepository extends IService<UserStat> {

    List<UserStat> scanUserStat(Page<UserStat> page);

    UserStat selectUserStatById(Integer userId);

    List<UserStat> selectUserStatByIds(List<Integer> userIds);

    List<HotCounter> loadAuthorRankList();

    boolean updateUserStat(UserStat stat);

    boolean updateArticles(Integer userId, int delta);

    @CanIgnoreReturnValue
    Long batchUpdateUserStats(List<UserStat> userStats);

    boolean updateFollowings(Integer userId, int delta);

    boolean updateFollowers(Integer userId, int delta);

    boolean updateViews(Integer userId, int delta);

    boolean updateLikes(Integer userId, int delta);

    boolean updateComments(Integer userId, int delta);

    boolean updateCollects(Integer userId, int delta);

}
