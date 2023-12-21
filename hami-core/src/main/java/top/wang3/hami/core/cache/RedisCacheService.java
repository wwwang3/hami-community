package top.wang3.hami.core.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.support.NullValue;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.lock.LockTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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


    private <T> T syncGet(String key, Supplier<T> loader, long timeout, TimeUnit timeUnit) {
        // 应该替换为分布式锁
        return lockTemplate.execute(key, () -> {
            T data = RedisClient.getCacheObject(key);
            if (data != null) return resolveValue(data);
            return loadCache(key, loader, timeout, timeUnit);
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

    private <T> T resolveValue(T value) {
        if (value instanceof NullValue) {
            return null;
        }
        return value;
    }
}
