package top.wang3.hami.core.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.support.NullValue;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.common.lock.LockTemplate;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class RedisCacheService implements CacheService {

    public static final long DEFAULT_EXPIRE = TimeUnit.DAYS.toMillis(1);

    public static final long EMPTY_OBJECT_EXPIRE = TimeUnit.SECONDS.toMillis(10);

    public static final Object EMPTY_OBJECT = NullValue.INSTANCE;


    private final LockTemplate lockTemplate;

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
        return data != null ? data : syncGetMapValue(key, hKey, loader, timeout, timeUnit);
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
    public boolean expireAndIncrBy(String key, int delta, long timeout, TimeUnit timeUnit) {
        if (RedisClient.expire(key, timeout, timeUnit)) {
            RedisClient.incrBy(key, delta);
            return true;
        }
        return false;
    }

    @Override
    public <T> void refreshCache(String key, T data) {
        refreshCache(key, data, DEFAULT_EXPIRE);
    }

    @Override
    public <T> void refreshCache(String key, T data, long mills) {
        lockTemplate.execute(
                key,
                () -> RedisClient.setCacheObject(key, data, mills, TimeUnit.MILLISECONDS)
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
                                  long timeout, TimeUnit timeUnit) {
        return lockTemplate.execute(key, () -> {
            T data = RedisClient.getCacheMapValue(key, hKey);
            return data != null ? data : loadHashCache(key, hKey, loader, timeout,timeUnit);
        });
    }

    private <T> T loadCache(String key, Supplier<T> loader, long timeout, TimeUnit timeUnit) {
        T data = loader.get();
        if (data == null) {
            RedisClient.setCacheObject(key, EMPTY_OBJECT, EMPTY_OBJECT_EXPIRE, TimeUnit.MILLISECONDS);
        } else {
            RedisClient.setCacheObject(key, data, timeout, timeUnit);
        }
        return data;
    }

    private <T> T loadHashCache(String key, String hKey,
                                Supplier<Map<String, T>> loader,
                                long timeout, TimeUnit timeUnit) {
        Map<String, T> hash = loader.get();
        RedisClient.hMSet(key, hash, timeout, timeUnit);
        return hash.get(hKey);
    }

    private <T> T resolveValue(T value) {
        if (value instanceof NullValue) {
            return null;
        }
        return value;
    }
}
