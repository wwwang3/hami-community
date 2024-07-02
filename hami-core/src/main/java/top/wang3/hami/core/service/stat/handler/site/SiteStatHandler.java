package top.wang3.hami.core.service.stat.handler.site;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.wang3.hami.canal.CanalEntryHandler;
import top.wang3.hami.canal.annotation.CanalRabbitHandler;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.constant.TimeoutConstants;
import top.wang3.hami.common.model.UserStat;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.cache.CacheService;
import top.wang3.hami.core.service.stat.CountService;

@Component
@CanalRabbitHandler(value = "user_stat", container = "canal-site-stat-container-1")
@Slf4j
@RequiredArgsConstructor
public class SiteStatHandler implements CanalEntryHandler<UserStat> {

    private final CacheService cacheService;
    private final CountService countService;

    @Override
    public void processInsert(UserStat entity) {
        // 插入了用户数据实体
        ensureCache();
        refreshExpire();
        cacheService.expireAndHIncrBy(RedisConstants.SITE_STAT, "users",1, TimeoutConstants.DEFAULT_EXPIRE);
    }

    @Override
    public void processUpdate(UserStat before, UserStat after) {
        if (isLogicDelete(before.getDeleted(), after.getDeleted())) {
            processDelete(after);
        } else {
            ensureCache();
            refreshExpire();
            int articles = after.getTotalArticles() - before.getTotalArticles();
            int views = after.getTotalViews() - before.getTotalViews();
            int likes = after.getTotalLikes() - before.getTotalLikes();
            int comments = after.getTotalComments() - before.getTotalComments();
            int collects = after.getTotalCollects() - before.getTotalCollects();
            String key = RedisConstants.SITE_STAT;
            RedisClient.hIncr(key, "articles", articles);
            RedisClient.hIncr(key, "views", views);
            RedisClient.hIncr(key, "likes", likes);
            RedisClient.hIncr(key, "comments", comments);
            RedisClient.hIncr(key, "collects", collects);
        }
    }

    @Override
    public void processDelete(UserStat deletedEntity) {
        ensureCache();
        refreshExpire();
        String key = RedisConstants.SITE_STAT;
        RedisClient.hIncr(key, "articles", -deletedEntity.getTotalArticles());
        RedisClient.hIncr(key, "views", -deletedEntity.getTotalViews());
        RedisClient.hIncr(key, "likes", -deletedEntity.getTotalLikes());
        RedisClient.hIncr(key, "comments", -deletedEntity.getTotalComments());
        RedisClient.hIncr(key, "collects", -deletedEntity.getTotalCollects());
    }

    private void ensureCache() {
        cacheService.getHashValue(RedisConstants.SITE_STAT,
            "users",
            countService::loadSiteStat,
            TimeoutConstants.DEFAULT_EXPIRE
        );
    }

    private void refreshExpire() {
        RedisClient.pExpire(RedisConstants.SITE_STAT, TimeoutConstants.DEFAULT_EXPIRE);
    }
}
