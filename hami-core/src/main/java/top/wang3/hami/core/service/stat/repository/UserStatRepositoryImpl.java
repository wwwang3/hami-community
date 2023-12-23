package top.wang3.hami.core.service.stat.repository;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import org.springframework.stereotype.Repository;
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
    public boolean updateArticles(Integer userId, int delta) {
        return ChainWrappers.updateChain(getBaseMapper())
                .set("articles = articles + ({0})", delta)
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
                .set("followings = followings + ({0})", delta)
                .eq("user_id", userId)
                .update();
    }

    @Override
    public boolean updateFollowers(Integer userId, int delta) {
        return ChainWrappers.updateChain(getBaseMapper())
                .set("followers = followers + ({0})", delta)
                .eq("user_id", userId)
                .update();
    }

    @Override
    public boolean updateViews(Integer userId, int delta) {
        return ChainWrappers.updateChain(getBaseMapper())
                .set("views = views + ({0})", delta)
                .eq("user_id", userId)
                .update();
    }

    @Override
    public boolean updateLikes(Integer userId, int delta) {
        return ChainWrappers.updateChain(getBaseMapper())
                .set("likes = likes + ({0})", delta)
                .eq("user_id", userId)
                .update();
    }

    @Override
    public boolean updateComments(Integer userId, int delta) {
        return ChainWrappers.updateChain(getBaseMapper())
                .set("comments = comments + ({0})", delta)
                .eq("user_id", userId)
                .update();
    }

    @Override
    public boolean updateCollects(Integer userId, int delta) {
        return ChainWrappers.updateChain(getBaseMapper())
                .set("collects = collects + ({0})", delta)
                .eq("user_id", userId)
                .update();
    }
}
