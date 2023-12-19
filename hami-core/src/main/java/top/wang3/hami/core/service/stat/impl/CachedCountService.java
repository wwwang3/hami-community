package top.wang3.hami.core.service.stat.impl;

import cn.hutool.core.date.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.constant.TimeoutConstants;
import top.wang3.hami.common.converter.StatConverter;
import top.wang3.hami.common.dto.stat.ArticleStatDTO;
import top.wang3.hami.common.dto.stat.UserStatDTO;
import top.wang3.hami.common.model.UserStat;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.annotation.CostLog;
import top.wang3.hami.core.service.interact.FollowService;
import top.wang3.hami.core.service.stat.ArticleStatService;
import top.wang3.hami.core.service.stat.CountService;
import top.wang3.hami.core.service.stat.UserStatService;
import top.wang3.hami.core.service.stat.repository.UserStatRepository;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CachedCountService implements CountService {

    private final ArticleStatService articleStatService;
    private final UserStatService userStatService;
    private final UserStatRepository userStatRepository;
    private final FollowService followService;

    @Override
    public ArticleStatDTO getArticleStatById(int articleId) {
        final String redisKey = RedisConstants.STAT_TYPE_ARTICLE + articleId;
        long timeout = TimeoutConstants.ARTICLE_STAT_EXPIRE;
        ArticleStatDTO dto;
        if (RedisClient.pExpire(redisKey, timeout)) {
            dto = RedisClient.getCacheObject(redisKey);
        } else {
            synchronized (redisKey.intern()) {
                dto = RedisClient.getCacheObject(redisKey);
                if (dto == null) {
                    dto = loadArticleStatCache(redisKey, articleId);
                }
            }
        }
        return dto;
    }

    @Override
    public UserStatDTO getUserStatDTOById(Integer userId) {
        final String key = RedisConstants.STAT_TYPE_USER + userId;
        long timeout = TimeoutConstants.USER_STAT_EXPIRE;
        UserStatDTO dto;
        if (RedisClient.pExpire(key, timeout)) {
            Map<String, Integer> map = RedisClient.getCacheMap(key);
            dto = StatConverter.INSTANCE.mapToUserStatDTO(map);
        } else {
            synchronized (key.intern()) {
                Map<String, Integer> map = RedisClient.getCacheMap(key);
                if (map.isEmpty()) {
                    // 仍然为空
                    dto = loadUserStatDTO(key, userId);
                } else {
                    dto = StatConverter.INSTANCE.mapToUserStatDTO(map);
                }
            }
        }
        return dto;
    }

    @CostLog
    @Override
    public Map<Integer, ArticleStatDTO> getArticleStatByIds(List<Integer> articleIds) {
        return RedisClient.getMultiCacheObjectToMap(RedisConstants.STAT_TYPE_ARTICLE, articleIds,
                this::loadArticleStateCaches);
    }

    @Override
    public Map<Integer, UserStatDTO> getUserStatDTOByUserIds(List<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) return Collections.emptyMap();
        Map<Integer, UserStatDTO> map = new HashMap<>();
        for (Integer userId : userIds) {
            // 一个个获取算了
            map.put(userId, getUserStatDTOById(userId));
        }
        return map;
    }

    @Override
    public Map<String, Integer> getUserDailyDataGrowing(Integer userId) {
        String date = DateUtil.formatDate(new Date());
        String key = RedisConstants.DATA_GROWING + date + ":" + userId;
        return RedisClient.hMGetAll(key);
    }

    private ArticleStatDTO loadArticleStatCache(String redisKey, Integer articleId) {
        ArticleStatDTO stat = articleStatService.getArticleStatByArticleId(articleId);
        if (stat == null) {
            RedisClient.cacheEmptyObject(redisKey, new ArticleStatDTO(articleId));
        } else {
            RedisClient.setCacheObject(
                    redisKey,
                    stat,
                    TimeoutConstants.ARTICLE_STAT_EXPIRE,
                    TimeUnit.MILLISECONDS
            );
        }
        return stat;
    }

    @Override
    public Map<Integer, ArticleStatDTO> loadArticleStateCaches(List<Integer> ids) {
        Map<Integer, ArticleStatDTO> dtoMap = articleStatService.listArticleStat(ids);
        Map<String, ArticleStatDTO> newMap = ListMapperHandler.listToMap(ids, id -> RedisConstants.STAT_TYPE_ARTICLE + id, (id) -> {
            return dtoMap.computeIfAbsent(id, ArticleStatDTO::new);
        });
        RedisClient.cacheMultiObject(newMap, 10, 50, TimeUnit.HOURS);
        return dtoMap;
    }

    @Override
    public UserStatDTO loadUserStatDTO(String key, Integer userId) {
        UserStat stat = userStatRepository.selectUserStatById(userId);
        RedisClient.hMSet(
                key,
                StatConverter.INSTANCE.userStatToMap(stat),
                TimeoutConstants.USER_STAT_EXPIRE,
                TimeUnit.MILLISECONDS
        );
        return StatConverter.INSTANCE.toUserStatDTO(stat);
    }
}
