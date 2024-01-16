package top.wang3.hami.core.service.stat.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.wang3.hami.canal.CanalEntryHandler;
import top.wang3.hami.canal.annotation.CanalRabbitHandler;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.constant.TimeoutConstants;
import top.wang3.hami.common.converter.StatConverter;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.cache.CacheService;


@Component
@CanalRabbitHandler(value = "article_stat", container = "canal-stat-container-1")
@Slf4j
@RequiredArgsConstructor
//todo 失败重试
public class ArticleStatCanalHandler implements CanalEntryHandler<ArticleStat> {

    private final CacheService cacheService;

    @Override
    public void processInsert(ArticleStat entity) {
        if (entity == null) return;
        // 在文章数据表插入了一条数据
        // 写入Redis
        setCache(entity);
        log.debug("insert to Redis success: {}", entity);
    }

    @Override
    public void processUpdate(ArticleStat before, ArticleStat after) {
        // 更新 (删除缓存感觉更好)
        setCache(after);
        log.debug("update to Redis success: before: {}, after: {}", before, after);
    }

    @Override
    public void processDelete(ArticleStat deletedEntity) {
        Integer articleId = deletedEntity.getArticleId();
        RedisClient.deleteObject(RedisConstants.STAT_TYPE_ARTICLE + articleId);
        log.debug("update to Redis success: deleted: {}", deletedEntity);
    }

    private void setCache(ArticleStat stat) {
        String redisKey = RedisConstants.STAT_TYPE_ARTICLE + stat.getArticleId();
        cacheService.refreshCache(
                redisKey,
                StatConverter.INSTANCE.toArticleStatDTO(stat),
                TimeoutConstants.ARTICLE_STAT_EXPIRE
        );
    }
}
