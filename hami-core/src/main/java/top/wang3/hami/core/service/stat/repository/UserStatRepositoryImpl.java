package top.wang3.hami.core.service.stat.repository;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Repository;
import top.wang3.hami.common.model.HotCounter;
import top.wang3.hami.common.model.UserStat;
import top.wang3.hami.core.mapper.UserStatMapper;

import java.util.List;

@Repository
public class UserStatRepositoryImpl extends ServiceImpl<UserStatMapper, UserStat>
        implements UserStatRepository {

    public static final String[] FIELDS = {
            "user_id", "total_followings", "total_articles", "total_views",
            "total_likes", "total_comments", "total_collects", "total_followers"
    };

    @Override
    public List<UserStat> scanUserStat(Page<UserStat> page) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select(FIELDS)
                .orderByDesc("user_id")
                .list(page);
    }

    @Override
    public UserStat selectUserStatById(Integer userId) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select(FIELDS)
                .one();
    }

    @Override
    public List<UserStat> selectUserStatByIds(List<Integer> userIds) {
        return ChainWrappers.queryChain(getBaseMapper())
                .select(FIELDS)
                .in("user_id", userIds)
                .list();
    }

    @Override
    public List<HotCounter> loadAuthorRankList() {
        return getBaseMapper().selectAuthorRankList();
    }

    @Override
    public boolean updateUserStat(UserStat stat) {
        return ChainWrappers.updateChain(getBaseMapper())
                .eq("user_id", stat.getUserId())
                .set(stat.getTotalArticles() != null, "set total_articles = total_articles + ({0})", stat.getTotalArticles())
                .set(stat.getTotalLikes() != null, "set total_likes = total_likes + ({0})", stat.getTotalLikes())
                .set(stat.getTotalComments() != null, "set total_comments = total_comments + ({0})", stat.getTotalComments())
                .set(stat.getTotalCollects() != null, "set total_collects = total_collects + ({0})", stat.getTotalCollects())
                .set(stat.getTotalViews() != null, "set total_views = total_views + ({0})", stat.getTotalViews())
                .update();
    }

    @Override
    public boolean updateArticles(Integer userId, int delta) {
        return ChainWrappers.updateChain(getBaseMapper())
                .set("total_articles = total_articles + ({0})", delta)
                .eq("user_id", userId)
                .update();
    }

    @Override
    public Long batchUpdateUserStats(List<UserStat> userStats) {
        return getBaseMapper().batchUpdateUserStat(userStats);
    }

    @Override
    public boolean updateFollowings(Integer userId, int delta) {
        return ChainWrappers.updateChain(getBaseMapper())
                .set("total_followings = total_followings + ({0})", delta)
                .eq("user_id", userId)
                .update();
    }

    @Override
    public boolean updateFollowers(Integer userId, int delta) {
        return ChainWrappers.updateChain(getBaseMapper())
                .set("total_followers = total_followers + ({0})", delta)
                .eq("user_id", userId)
                .update();
    }

    @Override
    public boolean updateViews(Integer userId, int delta) {
        return ChainWrappers.updateChain(getBaseMapper())
                .set("total_views = total_views + ({0})", delta)
                .eq("user_id", userId)
                .update();
    }

    @Override
    public boolean updateLikes(Integer userId, int delta) {
        return ChainWrappers.updateChain(getBaseMapper())
                .set("total_likes = total_likes + ({0})", delta)
                .eq("user_id", userId)
                .update();
    }

    @Override
    public boolean updateComments(Integer userId, int delta) {
        return ChainWrappers.updateChain(getBaseMapper())
                .set("total_comments = total_comments + ({0})", delta)
                .eq("user_id", userId)
                .update();
    }

    @Override
    public boolean updateCollects(Integer userId, int delta) {
        return ChainWrappers.updateChain(getBaseMapper())
                .set("total_collects = total_collects + ({0})", delta)
                .eq("user_id", userId)
                .update();
    }
}
