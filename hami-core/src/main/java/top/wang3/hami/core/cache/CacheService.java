package top.wang3.hami.core.cache;

import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public interface CacheService {

    /**
     * 获取缓存对象, 默认一天过期时间
     * @param key 对象key
     * @param loader 从db加载缓存的方法
     * @return 缓存对象
     * @param <T> 返回对象泛型
     */
    <T> T get(String key, Supplier<T> loader);

    /**
     * 获取缓存对象
     * @param key 对象key
     * @param loader 从db加载缓存的方法
     * @param millis 缓存过期时间
     * @return 缓存对象
     * @param <T> 返回对象泛型
     */
    @Nullable
    <T> T get(String key, Supplier<T> loader, long millis);

    <T> T getHashValue(String key, String hKey, Supplier<Map<String, T>> loader, long millis);

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

    boolean expireAndHIncrBy(String key, String hkey, int delta, long mills);

    /**
     * 刷新过期时间, 刷新失败则<b>加锁<b/>执行runnable
     * @param key key
     * @param millis 过期时间, 单位毫秒
     * @param runnable 刷新失败执行的方法
     */
    void expiredThenExecute(String key, long millis, Runnable runnable);

    <T> void asyncSetCache(String key, T data, long millis);

    <T, R> void asyncSetCacheAbsent(String keyPrefix, List<R> applied, Function<R, T> idMapper, long millis);

    /**
     * 异步写入缓存, 必须确保ids和applied元素对应上
     * @param keyPrefix
     * @param ids
     * @param applied
     * @param millis
     * @param <T>
     * @param <R>
     */
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
