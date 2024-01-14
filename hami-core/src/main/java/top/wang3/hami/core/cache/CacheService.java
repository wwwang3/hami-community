package top.wang3.hami.core.cache;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public interface CacheService {

    <T> T get(String key, Supplier<T> loader);

    <T> T get(String key, Supplier<T> loader, long timeout, TimeUnit timeUnit);

    <T> T getMapValue(String key, String hKey, Supplier<Map<String, T>> loader, long timeout, TimeUnit timeUnit);

    <T, R> List<R> multiGet(String keyPrefix, List<T> items, Function<T, R> loader, long timeout, TimeUnit timeUnit);

    <T, R> List<R> multiGetById(String keyPrefix, Collection<T> ids, Function<List<T>, List<R>> loader, long timeout, TimeUnit timeUnit);

    boolean expireAndIncrBy(String key, int delta, long timeout, TimeUnit timeUnit);

    <T, R> void asyncSetCache(String keyPrefix, List<T> ids, List<R> applied,
                              long timeout, TimeUnit timeUnit);

    /**
     * 加锁刷新缓存, 默认一天过期时间
     * @param key 缓存key
     * @param data data
     * @param <T> data泛型
     */
    <T> void refreshCache(String key, T data);

    /**
     * 加锁刷新缓存
     * @param key 缓存key
     * @param data data
     * @param mills 过期时间, 单位毫秒
     * @param <T> data泛型
     */
    <T> void refreshCache(String key, T data, long mills);

    /**
     * 加锁刷新hash缓存, 默认一天过期时间
     * @param key 缓存key
     * @param hash hash
     * @param <T> hash-value泛型
     */
    <T> void refreshHashCache(String key, Map<String, T> hash);

    /**
     * 加锁刷新hash缓存
     * @param key 缓存key
     * @param hash data
     * @param mills 过期时间, 单位毫秒
     * @param <T> hash-value泛型
     */
    <T> void refreshHashCache(String key, Map<String, T> hash, long mills);

}
