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
            stat = loadUserStatCache(redisKey, userId);
        }
        Long followingCount = followService.getUserFollowingCount(userId);
        Long followerCount = followService.getUserFollowerCount(userId);
        stat.setTotalFollowings(followingCount.intValue());
        stat.setTotalFollowers(followerCount.intValue());
        return stat;
    }

    @CostLog
    @Override
    public Map<Integer, UserStat> getUserStatByUserIds(List<Integer> userIds) {
        Map<Integer, UserStat> statMap = RedisClient.getMultiCacheObjectToMap(RedisConstants.STAT_TYPE_USER, userIds, this::loadUserStatCaches);
        Map<Integer, Long> followings = followService.listUserFollowingCount(userIds);
        Map<Integer, Long> followers = followService.listUserFollowerCount(userIds);
        statMap.forEach((userId, item) -> {
            Integer followingCount = followings.getOrDefault(userId, 0L).intValue();
            Integer followerCount = followers.getOrDefault(userId, 0L).intValue();
            item.setTotalFollowings(followingCount);
            item.setTotalFollowers(followerCount);
        });
        return statMap;
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

    private Map<Integer, ArticleStatDTO> loadArticleStateCaches(List<Integer> ids) {
        Map<Integer, ArticleStatDTO> dtoMap = articleStatService.listArticleStat(ids);
        Map<String, ArticleStatDTO> newMap = ListMapperHandler.listToMap(ids, id -> RedisConstants.STAT_TYPE_ARTICLE + id, (id) -> {
            return dtoMap.computeIfAbsent(id, ArticleStatDTO::new);
        });
        RedisClient.cacheMultiObject(newMap, 10, 20, TimeUnit.HOURS);
        return dtoMap;
    }

    private UserStat loadUserStatCache(String redisKey, Integer userId) {
        synchronized (this) {
            UserStat stat = RedisClient.getCacheObject(redisKey);
            //stat为空
            if (stat == null) {
                stat = articleStatService.getUserStatByUserId(userId);
                RedisClient.setCacheObject(redisKey, stat, RandomUtils.randomLong(10, 20), TimeUnit.HOURS);
            }
            return stat;
        }
    }

    private Map<Integer, UserStat> loadUserStatCaches(List<Integer> ids) {
        Map<Integer, UserStat> statMap = articleStatService.listUserStat(ids);
        Map<String, UserStat> cache = ListMapperHandler.listToMap(ids, id -> RedisConstants.STAT_TYPE_USER + id, id -> {
            return statMap.computeIfAbsent(id, UserStat::new);
        });
        RedisClient.cacheMultiObject(cache, 10, 20, TimeUnit.HOURS);
        return statMap;
    }


}
