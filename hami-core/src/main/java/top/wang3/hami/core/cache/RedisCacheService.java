package top.wang3.hami.core.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.support.NullValue;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.common.lock.LockTemplate;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisCacheService implements CacheService {

    public static final long DEFAULT_EXPIRE = TimeUnit.DAYS.toMillis(1);

    public static final long EMPTY_OBJECT_EXPIRE = TimeUnit.MINUTES.toMillis(1);

    public static final Object EMPTY_OBJECT = NullValue.INSTANCE;

    private final LockTemplate lockTemplate;

    private final ThreadPoolTaskExecutor executor;

    @Override
    public <T> T get(String key, Supplier<T> loader) {
        return get(key, loader, DEFAULT_EXPIRE, TimeUnit.MILLISECONDS);
    }

    @Override
    public <T> T get(String key, Supplier<T> loader, long timeout, TimeUnit timeUnit) {
        T data = RedisClient.getCacheObject(key);
        if (data != null) {
            return resolveValue(data);
        }
        return syncGet(key, loader, timeout, timeUnit);
    }

    @Override
    public <T> T getMapValue(String key, String hKey, Supplier<Map<String, T>> loader, long timeout, TimeUnit timeUnit) {
        T data = RedisClient.getCacheMapValue(key, hKey);
        long millis = timeUnit.toMillis(timeout);
        return data != null ? data : syncGetMapValue(key, hKey, loader, millis);
    }

    @Override
    public <T, R> List<R> multiGet(String keyPrefix, List<T> items, Function<T, R> loader,
                                   long timeout, TimeUnit timeUnit) {
        if (CollectionUtils.isEmpty(items)) {
            return Collections.emptyList();
        }
        List<String> keys = ListMapperHandler.listTo(items, i -> keyPrefix + i, false);
        List<R> dataList = RedisClient.getMultiCacheObject(keys);
        final ArrayList<R> result = new ArrayList<>(items.size());
        ListMapperHandler.forEach(items, (item, index) -> {
            R data = dataList.get(index);
            String key = keys.get(index);
            if (data == null) {
                final R applied = loader.apply(item);
                data = syncGet(key, () -> applied, timeout, timeUnit);
            }
            result.add(resolveValue(data));
        });
        return result;
    }

    @Override
    public <T, R> List<R> multiGetById(final String keyPrefix, Collection<T> ids, Function<List<T>, List<R>> loader, long timeout, TimeUnit timeUnit) {
        // 批量获取缓存数据
        // keys, 不能去重, 不然位置对不上
        List<String> keys = ListMapperHandler.listTo(ids, id -> keyPrefix + id, false);
        // data, 缓存过期时对应的key为空
        // 返回的是ArrayList
        List<R> dataList = RedisClient.getMultiCacheObject(keys);
        // 为空的Id
        ArrayList<T> nullIds = new ArrayList<>();
        // 结果
        ArrayList<R> results = new ArrayList<>(ids.size());
        ListMapperHandler.forEach(ids, (id, index) -> {
            R value = dataList.get(index);
            if (value == null) {
                nullIds.add(id);
            } else {
                // 可能缓存了空对象
                results.add(resolveValue(value));
            }
        });
        if (!nullIds.isEmpty()) {
            List<R> applied = loader.apply(nullIds);
            results.addAll(applied);
            // 异步写入缓存
            asyncSetCache(keyPrefix, nullIds, applied, timeout, timeUnit);
        }
        return results;
    }

    @Override
    public boolean expireAndIncrBy(String key, int delta, long timeout, TimeUnit timeUnit) {
        if (RedisClient.expire(key, timeout, timeUnit)) {
            RedisClient.incrBy(key, delta);
            return true;
        }
        return false;
    }

    @Override
    public void expiredThenExecute(String key, Runnable runnable, long mills) {
        // 刷新过期时间, 刷新失败则执行runnable
        if (!RedisClient.pExpire(key, mills)) {
            lockTemplate.execute(key, () -> {
                if (!RedisClient.pExpire(key, mills)) {
                    runnable.run();
                }
            });
        }
    }

    @Override
    public <T> void asyncSetCache(String key, T data, long timeout, TimeUnit timeUnit) {
        // 每个ID提交一个task?
        // 异步写入缓存
        executor.submitCompletable(() -> refreshCache(key, data, timeUnit.toMillis(timeout)))
                .whenComplete((rs, th) -> {
                    if (th != null) {
                        log.error(
                                "async refresh cache failed, key: {}, data: {}, error_msg: {}",
                                key,
                                data,
                                th.getMessage()
                        );
                    } else {
                        log.info("async refresh cache success, key: {}, data: {}", key, data);
                    }
                });
    }

    @Override
    public <T, R> void asyncSetCache(final String keyPrefix, List<T> ids, List<R> applied,
                                     long timeout, TimeUnit timeUnit) {
        // 每个ID提交一个task?
        // 异步写入缓存
        long millis = timeUnit.toMillis(timeout);
        executor.submitCompletable(() -> ListMapperHandler.forEach(applied, (item, index) -> {
            String key = keyPrefix + ids.get(index);
            // 刷新缓存
            refreshCache(key, item, millis);
        })).whenComplete((rs, th) -> {
            if (th != null) {
                log.error(
                        "async refresh cache failed, keyPrefix: {}, ids: {}, error_msg: {}",
                        keyPrefix,
                        ids,
                        th.getMessage()
                );
            } else {
                log.info("async refresh cache success, keyPrefix: {}, ids: {}", keyPrefix, ids);
            }
        });
    }

    @Override
    public <T> void asyncSetHashCache(String key, Map<String, T> hash, long timeout, TimeUnit timeUnit) {
        executor.submitCompletable(() -> refreshHashCache(key, hash, timeUnit.toMillis(timeout))).
                whenComplete((rs, th) -> {
                    if (th != null) {
                        log.error(
                                "async refresh cache failed, key: {}, hash: {}, error_msg: {}",
                                key,
                                hash,
                                th.getMessage()
                        );
                    } else {
                        log.info("async refresh cache success, key: {}, hash: {}", key, hash);
                    }
                });
    }

    @Override
    public <T> void refreshCache(String key, T data) {
        refreshCache(key, data, DEFAULT_EXPIRE);
    }

    @Override
    public <T> void refreshCache(String key, T data, long mills) {
        lockTemplate.execute(
                key,
                () -> setCache(key, data, mills)
        );
    }

    @Override
    public <T> void refreshHashCache(String key, Map<String, T> hash) {
        refreshHashCache(key, hash, DEFAULT_EXPIRE);
    }

    @Override
    public <T> void refreshHashCache(String key, Map<String, T> hash, long mills) {
        lockTemplate.execute(key, () -> RedisClient.hMSet(key, hash, mills, TimeUnit.MILLISECONDS));
    }

    private <T> T syncGet(String key, Supplier<T> loader, long timeout, TimeUnit timeUnit) {
        // 应该替换为分布式锁
        return lockTemplate.execute(key, () -> {
            T data = RedisClient.getCacheObject(key);
            if (data != null) return resolveValue(data);
            return loadCache(key, loader, timeout, timeUnit);
        });
    }

    private <T> T syncGetMapValue(String key, String hKey, Supplier<Map<String, T>> loader,
                                  long mills) {
        return lockTemplate.execute(key, () -> {
            T data = RedisClient.getCacheMapValue(key, hKey);
            return data != null ? data : loadHashCache(key, hKey, loader, mills);
        });
    }

    private <T> T loadCache(String key, Supplier<T> loader, long timeout, TimeUnit timeUnit) {
        T data = loader.get();
        setCache(key, data, timeUnit.toMillis(timeout));
        return data;
    }

    private <T> T loadHashCache(String key, String hKey,
                                Supplier<Map<String, T>> loader,
                                long mills) {
        Map<String, T> hash = loader.get();
        RedisClient.hMSet(key, hash, mills, TimeUnit.MILLISECONDS);
        return hash.get(hKey);
    }


    private <T> void setCache(String key, T data, long mills) {
        if (data == null) {
            RedisClient.setCacheObject(key, EMPTY_OBJECT, EMPTY_OBJECT_EXPIRE, TimeUnit.MILLISECONDS);
        } else {
            RedisClient.setCacheObject(key, data, mills, TimeUnit.MILLISECONDS);
        }
    }

    private <T> T resolveValue(T value) {
        if (value instanceof NullValue) {
            return null;
        }
        return value;
    }
}
