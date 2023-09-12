package top.wang3.hami.core.service.stat.impl;

import lombok.RequiredArgsConstructor;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.ArticleStatDTO;
import top.wang3.hami.common.dto.UserStat;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.service.article.ArticleStatService;
import top.wang3.hami.core.service.stat.CountService;
import top.wang3.hami.core.service.user.UserFollowService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class CachedCountServiceImpl implements CountService {

    private final ArticleStatService articleStatService;

    private final UserFollowService userFollowService;

    @Override
    public ArticleStatDTO getArticleStatById(int articleId) {
        String redisKey = Constants.COUNT_TYPE_ARTICLE + articleId;
        ArticleStatDTO stat = RedisClient.getCacheObject(redisKey);
        if (stat != null) {
            return stat;
        }
        return loadArticleStatCache(redisKey, articleId);
    }

    @Override
    public UserStat getUserStatById(Integer userId) {
        String redisKey = Constants.COUNT_TYPE_USER + userId;
        Map<String, Integer> data = RedisClient.hMGetAll(redisKey);
        UserStat stat = CountService.readUserStatFromMap(data, userId);
        if (stat != null) return stat;
        return loadUserStatCache(redisKey, userId);
    }

    @Override
    public List<ArticleStatDTO> getArticleStatByIds(List<Integer> articleIds) {
        ArrayList<ArticleStatDTO> dtos = new ArrayList<>();
        for (Integer articleId : articleIds) {
            ArticleStatDTO stat = getArticleStatById(articleId);
            dtos.add(stat);
        }
        return dtos;
    }

    @Override
    public List<UserStat> getUserStatByUserIds(List<Integer> userIds) {
        userIds = userIds.stream().distinct().toList();
        ArrayList<UserStat> stats = new ArrayList<>(userIds.size());
        for (Integer useId : userIds) {
            stats.add(getUserStatById(useId));
        }
        return stats;
    }

    private ArticleStatDTO loadArticleStatCache(String redisKey, Integer articleId) {
        //todo 加锁
        ArticleStatDTO articleStat = articleStatService.getArticleStatByArticleId(articleId);
        RedisClient.setCacheObject(redisKey, articleStat);
        return articleStat;
    }

    private UserStat loadUserStatCache(String redisKey, Integer userId) {
        //todo 加锁
        UserStat statistics = articleStatService.getUserStatistics(userId);
        Integer followings = userFollowService.getUserFollowingCount(userId);
        Integer followers = userFollowService.getUserFollowerCount(userId);
        statistics.setFollowings(followings);
        statistics.setFollowers(followers);
        Map<String, Integer> map = CountService.setUserStatToMap(statistics);
        RedisClient.hMSet(redisKey, map);
        return statistics;
    }
}
