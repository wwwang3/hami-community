package top.wang3.hami.core.cache;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public interface CacheService {

    <T> T get(String key, Supplier<T> loader);

    <T> T get(String key, Supplier<T> loader, long millis);

    <T> T getMapValue(String key, String hKey, Supplier<Map<String, T>> loader, long millis);

    /**
     *
     * @param keyPrefix 构建key的前缀, prefix+id = key
     * @param ids id列表
     * @param loader 某个ID缓存为空时调用的方法
     * @param millis 有效期, 单位ms
     * @return 缓存列表
     * @param <T> id泛型
     * @param <R> 缓存泛型
     */
    <T, R> List<R> multiGet(String keyPrefix, List<T> ids, Function<T, R> loader, long millis);

    /**
     * 根据Id批量获取缓存数据, 异步写入缓存
     * @param keyPrefix 构建key的前缀, prefix+id = key
     * @param ids id列表
     * @param loader 空ID列表对应的缓存查询方法
     * @param idMapper 将R映射为id
     * @param mills 有效期, 单位ms
     * @return 缓存列表
     * @param <T> id泛型
     * @param <R> 缓存泛型
     */
    <T, R> List<R> multiGetById(String keyPrefix, List<T> ids,
                                Function<List<T>, List<R>> loader,
                                Function<R, T> idMapper, long mills);

    /**
     * 刷新过期时间然后自增, 避免自增了过期的key
     * @param key key
     * @param delta delta
     * @param millis 有效期, 单位ms
     * @return 是否成功
     */
    boolean expireAndIncrBy(String key, int delta, long millis);

    /**
     * 刷新过期时间, 刷新失败则<b>加锁<b/>执行runnable
     * @param key key
     * @param runnable runnable
     * @param millis 过期时间, 单位毫秒
     */
    void expiredThenExecute(String key, Runnable runnable, long millis);

    <T> void asyncSetCache(String key, T data, long millis);

    <T, R> void asyncSetCacheAbsent(String keyPrefix, List<T> ids, List<R> applied, long millis);

    <T> void asyncSetHashCache(String key, Map<String, T> hash, long millis);

    /**
     * 加锁刷新缓存
     * @param key 缓存key
     * @param data data
     * @param millis 过期时间, 单位毫秒
     * @param <T> data泛型
     */
    <T> void refreshCache(String key, T data, long millis);

    <T> T refreshCacheAbsent(String key, Supplier<T> loader, long millis);

    /**
     * 加锁刷新hash缓存
     * @param key 缓存key
     * @param hash data
     * @param millis 过期时间, 单位毫秒
     * @param <T> hash-value泛型
     */
    <T> void refreshHashCache(String key, Map<String, T> hash, long millis);

    <T> Map<String, T> refreshHashCacheAbsent(String key, Supplier<Map<String, T>> loader, long millis);


}
