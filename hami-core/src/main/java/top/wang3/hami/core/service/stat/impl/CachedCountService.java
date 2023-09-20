package top.wang3.hami.core.service.stat.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.article.ArticleStatDTO;
import top.wang3.hami.common.dto.user.UserStat;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.annotation.CostLog;
import top.wang3.hami.core.service.stat.CountService;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class CachedCountService implements CountService {

    private final CountService origin;

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

    @CostLog
    @Override
    public List<ArticleStatDTO> getArticleStatByIds(List<Integer> articleIds) {
        List<String> keys = ListMapperHandler.listTo(articleIds, id -> {
            return Constants.COUNT_TYPE_ARTICLE + id;
        });
        return RedisClient.getMultiCacheObject(keys, (stat, index) -> {
            return this.getArticleStatById(articleIds.get(index));
        });
    }

    @CostLog
    @Override
    public List<UserStat> getUserStatByUserIds(List<Integer> userIds) {
        final List<String> keys = ListMapperHandler.listTo(userIds, id -> Constants.COUNT_TYPE_USER + id);
        final List<Map<String, Integer>> stats = RedisClient.hMGetAll(keys);
        return ListMapperHandler.listTo(stats, (stat, index) -> {
            UserStat data;
            if (stat == null || stat.isEmpty()) {
                data = loadUserStatCache(keys.get(index), userIds.get(index));
            } else {
                data = CountService.readUserStatFromMap(stat, userIds.get(index));
            }
            return data;
        });
    }

    private ArticleStatDTO loadArticleStatCache(String redisKey, Integer articleId) {
        //并发安全问题
        ArticleStatDTO stat = origin.getArticleStatById(articleId);
        RedisClient.setCacheObject(redisKey, stat);
        return stat;
    }

    private UserStat loadUserStatCache(String redisKey, Integer userId) {
        UserStat stat = origin.getUserStatById(userId);
        Map<String, Integer> map = CountService.setUserStatToMap(stat);
        RedisClient.hMSet(redisKey, map);
        return stat;
    }
}
