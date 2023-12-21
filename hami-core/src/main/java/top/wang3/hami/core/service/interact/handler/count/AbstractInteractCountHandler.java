package top.wang3.hami.core.service.interact.handler.count;

import top.wang3.hami.canal.CanalEntryHandler;
import top.wang3.hami.common.constant.TimeoutConstants;
import top.wang3.hami.core.cache.CacheService;

import java.util.concurrent.TimeUnit;

public abstract class AbstractInteractCountHandler<T> implements CanalEntryHandler<T> {

    private final CacheService cacheService;

    public AbstractInteractCountHandler(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public abstract String buildKey(T entity);

    public abstract boolean isInsert(T before, T after);

    protected void loadCount(T entity) {

    }

    protected long getExpireMills() {
        return TimeoutConstants.INTERACT_COUNT_EXPIRE;
    }


    @Override
    public void processInsert(T entity) {
        execute(entity, 1);
    }

    @Override
    public void processUpdate(T before, T after) {
        if (isInsert(before, after)) {
            execute(after, 1);
        } else {
            execute(after, -1);
        }
    }

    @Override
    public void processDelete(T deletedEntity) {
        execute(deletedEntity, -1);
    }


    protected void execute(T entity, int delta) {
        final String key = buildKey(entity);
        final long timeout = getExpireMills();
        boolean success = cacheService.expireAndIncrBy(
                key,
                delta,
                timeout,
                TimeUnit.MILLISECONDS
        );
        if (!success) {
            // 缓存过期
            loadCount(entity);
        }
    }
}
