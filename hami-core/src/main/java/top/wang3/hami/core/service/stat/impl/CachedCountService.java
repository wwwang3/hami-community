package top.wang3.hami.core.service.stat.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.constant.TimeoutConstants;
import top.wang3.hami.common.dto.stat.ArticleStatDTO;
import top.wang3.hami.common.dto.stat.UserStatDTO;
import top.wang3.hami.common.util.DateUtils;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.annotation.CostLog;
import top.wang3.hami.core.cache.CacheService;
import top.wang3.hami.core.service.interact.FollowService;
import top.wang3.hami.core.service.stat.ArticleStatService;
import top.wang3.hami.core.service.stat.CountService;
import top.wang3.hami.core.service.stat.UserStatService;
import top.wang3.hami.core.service.stat.repository.UserStatRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CachedCountService implements CountService {

    private final ArticleStatService articleStatService;
    private final UserStatService userStatService;
    private final UserStatRepository userStatRepository;
    private final FollowService followService;
    private final CacheService cacheService;

    @Override
    public ArticleStatDTO getArticleStatById(int articleId) {
        final String redisKey = RedisConstants.STAT_TYPE_ARTICLE + articleId;
        long timeout = TimeoutConstants.ARTICLE_STAT_EXPIRE;
        return cacheService.get(
                redisKey,
                () -> getArticleStatById(articleId),
                timeout,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public UserStatDTO getUserStatDTOById(Integer userId) {
        final String key = RedisConstants.STAT_TYPE_USER + userId;
        long timeout = TimeoutConstants.USER_STAT_EXPIRE;
        return cacheService.get(
                key,
                () -> userStatService.getUserStatDTOById(userId),
                timeout,
                TimeUnit.MILLISECONDS
        );
    }

    @CostLog
    @Override
    public Map<Integer, ArticleStatDTO> getArticleStatByIds(List<Integer> articleIds) {
        List<ArticleStatDTO> dtos = cacheService.multiGet(
                RedisConstants.STAT_TYPE_ARTICLE,
                articleIds,
                articleStatService::getArticleStatByArticleId,
                TimeoutConstants.ARTICLE_STAT_EXPIRE,
                TimeUnit.MILLISECONDS
        );
        return ListMapperHandler.listToMap(dtos, ArticleStatDTO::getArticleId);
    }

    @Override
    public Map<Integer, UserStatDTO> getUserStatDTOByUserIds(List<Integer> userIds) {
        List<UserStatDTO> dtos = cacheService.multiGet(
                RedisConstants.STAT_TYPE_USER,
                userIds,
                userStatService::getUserStatDTOById,
                TimeoutConstants.USER_STAT_EXPIRE,
                TimeUnit.MILLISECONDS
        );
        return ListMapperHandler.listToMap(dtos, UserStatDTO::getUserId);
    }

    @Override
    @NonNull
    public Integer getUserArticleCount(Integer userId) {
        UserStatDTO dto = getUserStatDTOById(userId);
        return Optional.ofNullable(dto)
                .map(UserStatDTO::getTotalArticles)
                .orElse(0);
    }

    @Override
    @NonNull
    public Integer getUserFollowingCount(Integer userId) {
        UserStatDTO dto = getUserStatDTOById(userId);
        return Optional.ofNullable(dto)
                .map(UserStatDTO::getTotalFollowings)
                .orElse(0);
    }

    @Override
    @NonNull
    public Integer getUserFollowerCount(Integer userId) {
        UserStatDTO dto = getUserStatDTOById(userId);
        return Optional.ofNullable(dto)
                .map(UserStatDTO::getTotalFollowers)
                .orElse(0);
    }

    @Override
    public Map<String, Integer> getUserDailyDataGrowing(Integer userId) {
        String date = DateUtils.formatDate(System.currentTimeMillis());
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
    public void loadUserStatCaches(List<Integer> userIds) {
        List<UserStatDTO> dtos = userStatService.getUserStatDTOByIds(userIds);
        Map<String, UserStatDTO> dtoMap = ListMapperHandler.listToMap(
                dtos,
                dto -> RedisConstants.STAT_TYPE_USER + dto.getUserId()
        );
        RedisClient.cacheMultiObject(dtoMap);
    }

    @Override
    public UserStatDTO loadUserStatDTO(String key, Integer userId) {
        return userStatService.getUserStatDTOById(userId);
    }
}
