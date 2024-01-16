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
import top.wang3.hami.core.service.stat.ArticleStatService;
import top.wang3.hami.core.service.stat.CountService;
import top.wang3.hami.core.service.stat.UserStatService;

import java.util.Date;
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
    private final CacheService cacheService;

    @Override
    public ArticleStatDTO getArticleStatById(int articleId) {
        final String redisKey = RedisConstants.STAT_TYPE_ARTICLE + articleId;
        long timeout = TimeoutConstants.ARTICLE_STAT_EXPIRE;
        return cacheService.get(
                redisKey,
                () -> articleStatService.getArticleStatId(articleId),
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
        List<ArticleStatDTO> dtos = cacheService.multiGetById(
                RedisConstants.STAT_TYPE_ARTICLE,
                articleIds,
                articleStatService::listArticleStatById,
                TimeoutConstants.ARTICLE_STAT_EXPIRE,
                TimeUnit.MILLISECONDS
        );
        return ListMapperHandler.listToMap(dtos, ArticleStatDTO::getArticleId);
    }

    @Override
    public Map<Integer, UserStatDTO> getUserStatDTOByUserIds(List<Integer> userIds) {
        List<UserStatDTO> dtos = cacheService.multiGetById(
                RedisConstants.STAT_TYPE_USER,
                userIds,
                userStatService::getUserStatDTOByIds,
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
    public Map<String, Integer> getUserYesterdayDataGrowing(Integer userId) {
        long yesterday = DateUtils.minusHours(new Date(), 24);
        String date = DateUtils.formatDate(yesterday);
        String key = RedisConstants.DATA_GROWING + date + ":" + userId;
        return RedisClient.hMGetAll(key);
    }
}
