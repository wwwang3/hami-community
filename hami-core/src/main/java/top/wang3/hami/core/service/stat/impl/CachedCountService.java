package top.wang3.hami.core.service.stat.impl;

import cn.hutool.core.date.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.dto.article.ArticleStatDTO;
import top.wang3.hami.common.dto.user.UserStat;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RandomUtils;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.annotation.CostLog;
import top.wang3.hami.core.service.article.ArticleStatService;
import top.wang3.hami.core.service.interact.FollowService;
import top.wang3.hami.core.service.stat.CountService;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CachedCountService implements CountService {

    private final ArticleStatService articleStatService;
    private final FollowService followService;

    @Override
    public ArticleStatDTO getArticleStatById(int articleId) {
        String redisKey = RedisConstants.STAT_TYPE_ARTICLE + articleId;
        ArticleStatDTO stat = RedisClient.getCacheObject(redisKey);
        if (stat != null) {
            return stat;
        }
        return loadArticleStatCache(redisKey, articleId);
    }

    @CostLog
    @Override
    public Map<Integer, ArticleStatDTO> getArticleStatByIds(List<Integer> articleIds) {
        return RedisClient.getMultiCacheObjectToMap(RedisConstants.STAT_TYPE_ARTICLE, articleIds,
                this::loadArticleStateCaches);
    }

    @Override
    public UserStat getUserStatById(Integer userId) {
        String redisKey = RedisConstants.STAT_TYPE_USER + userId;
        UserStat stat = RedisClient.getCacheObject(redisKey);
        if (stat == null) {
            synchronized (this) {
                stat = RedisClient.getCacheObject(redisKey);
                if (stat == null) {
                    stat = this.loadUserStatCache(userId);
                    RedisClient.setCacheObject(redisKey, stat, RandomUtils.randomLong(10, 100), TimeUnit.HOURS);
                }
            }
        }
        return stat;
    }

    @CostLog
    @Override
    public Map<Integer, UserStat> getUserStatByUserIds(List<Integer> userIds) {
        return RedisClient.getMultiCacheObjectToMap(RedisConstants.STAT_TYPE_USER, userIds, this::loadUserStatCaches);
    }

    @Override
    public Map<String, Integer> getUserDailyDataGrowing(Integer userId) {
        String date = DateUtil.formatDate(new Date());
        String key = RedisConstants.DATA_GROWING + date + ":" + userId;
        return RedisClient.hMGetAll(key);
    }

    private ArticleStatDTO loadArticleStatCache(String redisKey, Integer articleId) {
        synchronized (this) {
            ArticleStatDTO stat = RedisClient.getCacheObject(redisKey);
            if (stat != null) {
                return stat;
            }
            stat = articleStatService.getArticleStatByArticleId(articleId);
            if (stat == null) {
                RedisClient.cacheEmptyObject(redisKey, new ArticleStatDTO(articleId));
            } else {
                RedisClient.setCacheObject(redisKey, stat, RandomUtils.randomLong(10, 20), TimeUnit.HOURS);
            }
            return stat;
        }
    }

    @Override
    public Map<Integer, ArticleStatDTO> loadArticleStateCaches(List<Integer> ids) {
        Map<Integer, ArticleStatDTO> dtoMap = articleStatService.listArticleStat(ids);
        Map<String, ArticleStatDTO> newMap = ListMapperHandler.listToMap(ids, id -> RedisConstants.STAT_TYPE_ARTICLE + id, (id) -> {
            return dtoMap.computeIfAbsent(id, ArticleStatDTO::new);
        });
        RedisClient.cacheMultiObject(newMap, 10, 100, TimeUnit.HOURS);
        return dtoMap;
    }

    private UserStat loadUserStatCache(Integer userId) {
        UserStat stat = articleStatService.getUserStatByUserId(userId);
        Long followingCount = followService.getUserFollowingCount(userId);
        Long followerCount = followService.getUserFollowerCount(userId);
        stat.setTotalFollowings(followingCount.intValue());
        stat.setTotalFollowers(followerCount.intValue());
        return stat;
    }

    @Override
    public Map<Integer, UserStat> loadUserStatCaches(List<Integer> ids) {
        Map<Integer, UserStat> statMap = articleStatService.listUserStat(ids);
        //关注和粉丝数据
        final Map<Integer, Long> followings = followService.listUserFollowingCount(ids);
        final Map<Integer, Long> followers = followService.listUserFollowerCount(ids);
        statMap.forEach((userId, item) -> {
            Integer followingCount = followings.getOrDefault(userId, 0L).intValue();
            Integer followerCount = followers.getOrDefault(userId, 0L).intValue();
            item.setTotalFollowings(followingCount);
            item.setTotalFollowers(followerCount);
        });
        Map<String, UserStat> cache = ListMapperHandler.listToMap(ids, id -> RedisConstants.STAT_TYPE_USER + id, statMap::get);
        RedisClient.cacheMultiObject(cache, 10, 10, TimeUnit.HOURS);
        return statMap;
    }


}
