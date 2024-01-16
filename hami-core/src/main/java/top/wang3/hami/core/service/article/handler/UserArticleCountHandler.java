package top.wang3.hami.core.service.article.handler;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wang3.hami.canal.CanalEntryHandler;
import top.wang3.hami.canal.annotation.CanalRabbitHandler;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.constant.TimeoutConstants;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.common.util.RandomUtils;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.cache.CacheService;

import java.util.concurrent.TimeUnit;

@CanalRabbitHandler(value = "article", container = "canal-article-container-1")
@Component
@RequiredArgsConstructor
public class UserArticleCountHandler implements CanalEntryHandler<Article> {

    private final CacheService cacheService;

    @Override
    public void processInsert(Article entity) {
        handleInsertOrDelete(entity, 1);
    }

    @Override
    public void processUpdate(Article before, Article after) {
        Byte old = before.getDeleted();
        Byte now = after.getDeleted();
        if (isLogicDelete(old, now)) {
            handleInsertOrDelete(after, -1);
        }
    }

    @Override
    public void processDelete(Article deletedEntity) {
        handleInsertOrDelete(deletedEntity, -1);
    }

    private void handleInsertOrDelete(Article entity, int delta) {
        Integer userId = entity.getUserId();
        String key = RedisConstants.USER_ARTICLE_COUNT + userId;
        // 避免缓存过期
        cacheService.expireAndIncrBy(
                key,
                delta,
                TimeoutConstants.USER_ARTICLE_LIST_EXPIRE,
                TimeUnit.MILLISECONDS
        );
        if (RedisClient.expire(key, RandomUtils.randomLong(10, 100), TimeUnit.HOURS)) {
            RedisClient.incrBy(key, delta);
        }
    }
}
