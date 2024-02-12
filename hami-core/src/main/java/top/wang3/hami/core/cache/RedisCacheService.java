package top.wang3.hami.core.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.support.NullValue;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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
@Slf4j
public class RedisCacheService implements CacheService {

    public static final long DEFAULT_EXPIRE = TimeUnit.DAYS.toMillis(1);

    public static final long EMPTY_OBJECT_EXPIRE = TimeUnit.SECONDS.toMillis(20);

    public static final Object EMPTY_OBJECT = NullValue.INSTANCE;

    public final byte[] EMPTY_OBJECT_BYTE;

    private final LockTemplate lockTemplate;

    private final ThreadPoolTaskExecutor executor;

    @SuppressWarnings("all")
    public RedisCacheService(LockTemplate lockTemplate, ThreadPoolTaskExecutor executor,
                             @Qualifier("redisTemplate") RedisTemplate template) {
        this.lockTemplate = lockTemplate;
        this.executor = executor;
        EMPTY_OBJECT_BYTE = template.getValueSerializer().serialize(EMPTY_OBJECT);
    }

    @Override
    public <T> T get(String key, Supplier<T> loader) {
        return get(key, loader, DEFAULT_EXPIRE);
    }

    @Override
    public <T> T get(String key, Supplier<T> loader, long millis) {
        T data = RedisClient.getCacheObject(key);
        if (data != null) {
            return resolveValue(data);
        }
        return syncGet(key, loader, millis);
    }

    @Override
    public <T> T getHashValue(String key, String hKey, Supplier<Map<String, T>> loader, long millis) {
        T data = RedisClient.getCacheMapValue(key, hKey);
        return data != null ? data : syncGetMapValue(key, hKey, loader, millis);
    }

    @Override
    public <T, R> List<R> multiGet(String keyPrefix, List<T> items, Function<T, R> loader, long millis) {
        if (CollectionUtils.isEmpty(items)) {
            return Collections.emptyList();
        }
        List<String> keys = ListMapperHandler.listTo(items, i -> keyPrefix + i, false);
        List<R> dataList = RedisClient.getMultiCacheObject(keys);
        ListMapperHandler.forEach(items, (item, index) -> {
            R data = dataList.get(index);
            String key = keys.get(index);
            if (data == null) {
                // 同步获取并刷新缓存, 此前已经认为不存在
                data = syncGet(key, () -> loader.apply(item), millis);
                dataList.set(index, data);
            }
        });
        return dataList;
    }

    @Override
    public <T, R> List<R> multiGetById(final String keyPrefix,
                                       List<T> ids,
                                       Function<List<T>, List<R>> loader,
                                       Function<R, T> idMapper,
                                       final long millis) {
        // 批量获取缓存数据
        // keys, 不能去重, 不然位置对不上
        List<String> keys = ListMapperHandler.listTo(ids, id -> keyPrefix + id, false);
        // data, 当不存在key时对应index为空
        // 返回的是ArrayList
        List<R> dataList = RedisClient.getMultiCacheObject(keys);
        if (dataList == null || dataList.isEmpty()) {
            return Collections.emptyList();
        }
        // 为空的Id
        ArrayList<T> nullIds = new ArrayList<>();
        ArrayList<R> rs = new ArrayList<>(dataList.size());
        ListMapperHandler.forEach(ids, (id, index) -> {
            R data = dataList.get(index);
            if (data == null) {
                nullIds.add(id);
            } else {
                // 可能缓存的是NullValue
                rs.add(resolveValue(data));
            }
        });
        if (!nullIds.isEmpty() && loader != null) {
            List<R> applied = loader.apply(nullIds);
            rs.addAll(applied);
            // 异步写入缓存
            asyncSetCacheAbsent(keyPrefix, applied, idMapper, millis);
        }
        return rs;
    }

    @Override
    public boolean expireAndIncrBy(String key, int delta, long millis) {
        if (RedisClient.pExpire(key, millis)) {
            RedisClient.incrBy(key, delta);
            return true;
        }
        return false;
    }

    @Override
    public boolean expireAndHIncrBy(String key, String hkey, int delta, long mills) {
        if (RedisClient.pExpire(key, mills) && RedisClient.getCacheMapValue(key, hkey) != null) {
            RedisClient.hIncr(key, hkey, delta);
            return true;
        }
        return false;
    }

    @Override
    public void expiredThenExecute(String key, long millis, Runnable runnable) {
        // 刷新过期时间, 刷新失败则执行runnable
        if (!RedisClient.pExpire(key, millis)) {
            lockTemplate.execute(key, () -> {
                if (!RedisClient.pExpire(key, millis)) {
                    runnable.run();
                }
            });
        }
    }

    @Override
    public <T> void asyncSetCache(String key, T data, long millis) {
        // 异步写入缓存
        executor.submitCompletable(() -> refreshCache(key, data, millis))
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

    @SuppressWarnings("unchecked")
    @Override
    public <T, R> void asyncSetCacheAbsent(String keyPrefix, List<R> applied, Function<R, T> idMapper, long millis) {
        // 异步写入
        executor.submitCompletable(() -> {
            RedisClient.getTemplate()
                    .executePipelined((RedisCallback<Object>) connection -> {
                        // 不存在才写入, 因为这里的数据可能已经和db不一致了
                        // 这里极端情况下会出现写了旧的, 写不了新的, 但好像不影响
                        // 比如 a线程读取[a], b线程读取[b], b既然读到了[b], 说明db更新, 一般会有删除缓存的操作,
                        // 除非a写入缓存的操作时间 > db更新时间+ b写入缓存时间+db更新后删除缓存的时间
                        for (R item : applied) {
                            byte[] keyBytes = RedisClient.keyBytes(keyPrefix + idMapper.apply(item));
                            if (item != null) {
                                byte[] valueBytes = RedisClient.valueBytes(item);
                                connection.stringCommands().pSetEx(keyBytes, millis, valueBytes);
                            } else {
                                connection.stringCommands().pSetEx(keyBytes, EMPTY_OBJECT_EXPIRE, EMPTY_OBJECT_BYTE);
                            }
                        }
                        return null;
                    });
        }).whenComplete((rs, th) -> {
            if (th != null) {
                log.error(
                        "async refresh cache failed, items: {}, error_class: {}, error_msg: {}",
                        applied,
                        th.getClass().getName(),
                        th.getMessage()
                );
            } else {
                log.info("async refresh cache success, items: {}", applied);
            }
        });
    }

    @Override
    public <T, R> void asyncSetCacheAbsent(String keyPrefix, List<T> ids, List<R> applied, long millis) {
        // 异步写入
        executor.submitCompletable(() -> ListMapperHandler.forEach(applied, (item, index) -> {
            String key = keyPrefix + ids.get(index);
            // 不存在才写入, 因为这里的数据可能已经和db不一致了
            // 这里极端情况下会出现写了旧的, 写不了新的, 但好像不影响
            // 比如 a线程读取[a], b线程读取[b], b既然读到了[b], 说明db更新, 一般会有删除缓存的操作,
            // 除非a写入缓存的操作时间 > db更新时间+ b写入缓存时间+db更新后删除缓存的时间
            setCacheAbsent(key, item, millis);
        })).whenComplete((rs, th) -> {
            if (th != null) {
                log.error(
                        "async refresh cache failed, items: {}, error_class: {}, error_msg: {}",
                        applied,
                        th.getClass().getName(),
                        th.getMessage()
                );
            } else {
                log.info("async refresh cache success, items: {}", applied);
            }
        });
    }

    @Override
    public <T> void asyncSetHashCache(String key, Map<String, T> hash, long millis) {
        executor.submitCompletable(() -> refreshHashCache(key, hash, millis)).
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
    public <T> void refreshCache(String key, T data, long millis) {
        lockTemplate.execute(
                key,
                () -> setCache(key, data, millis)
        );
    }

    @Override
    public <T> T refreshCacheAbsent(String key, Supplier<T> loader, long millis) {
        // 加锁同步获取, 不存在时则调用loader刷新缓存
        return syncGet(key, loader, millis);
    }

    @Override
    public <T> void refreshHashCache(String key, Map<String, T> hash, long millis) {
        lockTemplate.execute(key, () -> RedisClient.hMSet(key, hash, millis, TimeUnit.MILLISECONDS));
    }

    @Override
    public <T> Map<String, T> refreshHashCacheAbsent(String key, Supplier<Map<String, T>> loader, long millis) {
        return lockTemplate.execute(key, () -> {
            Map<String, T> hash = RedisClient.hMGetAll(key);
            if (hash != null) {
                return hash;
            }
            hash = loader.get();
            RedisClient.hMSet(key, hash, millis, TimeUnit.MILLISECONDS);
            return hash;
        });
    }

    /**
     * 同步获取并刷新缓存
     *
     * @param key    key
     * @param loader loader
     * @param millis millis
     * @param <T>    data泛型
     * @return data
     */
    private <T> T syncGet(String key, Supplier<T> loader, long millis) {
        // 应该替换为分布式锁
        return lockTemplate.execute(key, () -> {
            // double check, 避免缓存击穿
            T data = RedisClient.getCacheObject(key);
            if (data != null) return resolveValue(data);
            data = loader.get();
            setCache(key, data, millis);
            return data;
        });
    }

    private <T> T syncGetMapValue(String key, String hKey, Supplier<Map<String, T>> loader,
                                  long millis) {
        return lockTemplate.execute(key, () -> {
            T data = RedisClient.getCacheMapValue(key, hKey);
            return data != null ? data : loadHashCache(key, hKey, loader, millis);
        });
    }

    private <T> T loadHashCache(String key, String hKey,
                                Supplier<Map<String, T>> loader,
                                long millis) {
        Map<String, T> hash = loader.get();
        RedisClient.hMSet(key, hash, millis, TimeUnit.MILLISECONDS);
        return hash.get(hKey);
    }

    private <T> void setCache(String key, T data, long millis) {
        if (data == null) {
            RedisClient.setCacheObject(key, EMPTY_OBJECT, EMPTY_OBJECT_EXPIRE, TimeUnit.MILLISECONDS);
        } else {
            RedisClient.setCacheObject(key, data, millis, TimeUnit.MILLISECONDS);
        }
    }

    @SuppressWarnings("unused")
    private <T> void setCacheAbsent(String key, T data, long millis) {
        if (data == null) {
            RedisClient.setCacheObject(key, EMPTY_OBJECT, EMPTY_OBJECT_EXPIRE, TimeUnit.MILLISECONDS);
        } else {
            RedisClient.setNx(key, data, millis, TimeUnit.MILLISECONDS);
        }
    }

    private <T> T resolveValue(T value) {
        if (value instanceof NullValue) {
            return null;
        }
        return value;
    }
}
