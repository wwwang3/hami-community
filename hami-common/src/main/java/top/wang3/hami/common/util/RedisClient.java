package top.wang3.hami.common.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

/**
 * Redis工具类
 */
@SuppressWarnings(value = {"unchecked", "rawtypes", "unused"})
public class RedisClient {

    private static RedisTemplate redisTemplate;

    public static void register(RedisTemplate template) {
        redisTemplate = template;
    }

    public static RedisTemplate getTemplate() {
        return redisTemplate;
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key   缓存的键值
     * @param value 缓存的值
     */
    public static <T> void setCacheObject(final String key, final T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key     缓存的键
     * @param value   缓存的对象
     * @param timeout 缓存时间，默认单位为秒
     * @param <T>     缓存对象的泛型
     */
    public static <T> void setCacheObject(final String key, final T value, final long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key      缓存的键值
     * @param value    缓存的值
     * @param timeout  有效期
     * @param timeUnit 时间颗粒度
     */
    public static <T> void setCacheObject(final String key, final T value,
                                          final long timeout, final TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    public static <T> boolean setNx(String key, final T value, final long timeout, TimeUnit timeUnit) {
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, value, timeout, timeUnit);
        return Boolean.TRUE.equals(success);
    }

    /**
     * 设置有效时间
     *
     * @param key     Redis键
     * @param timeout 超时时间
     * @return true=设置成功；false=设置失败
     */
    public static boolean expire(final String key, final long timeout) {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 设置有效时间
     *
     * @param key     Redis键
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return true=设置成功；false=设置失败
     */
    public static boolean expire(final String key, final long timeout, final TimeUnit unit) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
    }

    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据, key不存在返回空
     */
    public static <T> T getCacheObject(final String key) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

    public static <T> List<T> getMultiCacheObject(final List<String> keys) {
        Assert.notNull(keys, "keys cannot be null");
        Assert.isTrue(!keys.isEmpty() && keys.size() <= 20, "keys size must in [0, 20]");
        return redisTemplate.opsForValue()
                .multiGet(keys);
    }

    public static <T> List<T> getMultiCacheObject(final List<String> keys, BiFunction<String, Integer, T> func) {
        List<T> data = redisTemplate.opsForValue()
                .multiGet(keys);
        if (data == null || data.isEmpty()) return Collections.emptyList();
        for (int i = 0; i < keys.size(); i++) {
            if (data.get(i) == null) {
                data.set(i, func.apply(keys.get(i), i));
            }
        }
        return data;
    }

    /**
     * 删除单个对象
     *
     * @param key 缓存的key
     */
    public static boolean deleteObject(final String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    /**
     * 删除多个key对应的对象
     *
     * @param collection 多个对象
     * @return 删除的个数
     */
    public static long deleteObject(final Collection collection) {
        Long counts = redisTemplate.delete(collection);
        return counts == null ? 0L : counts;
    }

    /**
     * 缓存List数据
     *
     * @param key      缓存的键值
     * @param dataList 待缓存的List数据
     * @return 缓存的对象
     */
    public static <T> long setCacheList(final String key, final List<T> dataList) {
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        return count == null ? 0 : count;
    }

    public static <T> long setCacheList(final String key, final List<T> dataList, long timeout) {
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        redisTemplate.expire(key, Duration.ofSeconds(timeout));
        return count == null ? 0 : count;
    }

    /**
     * 获得缓存的list对象
     *
     * @param key 缓存的键值
     * @return 缓存键值对应的数据, key不存在时会返回空列表
     */
    public static <T> List<T> getCacheList(final String key) {
        return (List<T>) redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 获得缓存的list对象
     *
     * @param key   缓存的键值
     * @param start 其实索引
     * @param end   结束索引
     * @return 缓存键值对应的数据, key不存在时会返回空列表
     */
    public static <T> List<T> getCacheList(final String key, long start, long end) {
        return (List<T>) redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 获取缓存的list的大小
     *
     * @param key 缓存key
     * @return list大小
     */
    public static Long getCacheListSize(final String key) {
        return redisTemplate.opsForList().size(key);
    }

    /**
     * 缓存Set
     *
     * @param key     缓存键值
     * @param dataSet 缓存的数据
     * @return 缓存数据的对象
     */
    public static <T> BoundSetOperations<String, T> setCacheSet(final String key, final Set<T> dataSet) {
        BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
        for (T t : dataSet) {
            setOperation.add(t);
        }
        return setOperation;
    }

    /**
     * 获得缓存的set
     *
     * @param key 缓存key
     * @return 缓存的Set集合对象
     */
    public static <T> Set<T> getCacheSet(final String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 缓存Map
     *
     * @param key     缓存key
     * @param dataMap 待缓存的Map集合对象
     */
    public static <T> void setCacheMap(final String key, final Map<String, T> dataMap) {
        if (dataMap != null) {
            redisTemplate.opsForHash().putAll(key, dataMap);
        }
    }

    /**
     * 获得缓存的Map
     *
     * @param key 缓存key
     * @return 缓存Map集合对象
     */
    public static <T> Map<String, T> getCacheMap(final String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 往Hash中存入数据
     *
     * @param key   Redis键
     * @param hKey  Hash键
     * @param value 值
     */
    public static <T> void setCacheMapValue(final String key, final String hKey, final T value) {
        redisTemplate.opsForHash().put(key, hKey, value);
    }

    /**
     * 获取Hash中的数据
     *
     * @param key     Redis键
     * @param hashKey Hash键
     * @return Hash中的对象
     */
    public static <T> T getCacheMapValue(final String key, final String hashKey) {
        HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
        return opsForHash.get(key, hashKey);
    }

    /**
     * 删除Hash中的数据
     *
     * @param key     缓存key
     * @param hashKey 缓存hashmap key
     */
    public static void delCacheMapValue(final String key, final String hashKey) {
        HashOperations hashOperations = redisTemplate.opsForHash();
        hashOperations.delete(key, hashKey);
    }

    /**
     * 获取多个Hash中的数据
     *
     * @param key   Redis键
     * @param hKeys Hash键集合
     * @return Hash对象集合
     */
    public static <T> List<T> getMultiCacheMapValue(final String key, final Collection<Object> hKeys) {
        return redisTemplate.opsForHash().multiGet(key, hKeys);
    }

    /**
     * 获得缓存的基本对象列表
     *
     * @param pattern 字符串前缀
     * @return 对象列表
     */
    public static Collection<String> keys(final String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 以秒为单位获取密钥的生存时间。
     *
     * @param key 缓存对象的key
     * @return 剩余的过期时间 单位为秒
     * redisTemplate.getExpire
     * 返回-2 表示key不存在
     * 返回-1 表示键没有设置过期时间 这两种情况都返回0
     */
    public static long getExpire(String key) {
        Long ttl = redisTemplate.getExpire(key);
        return ttl == null || ttl < 0 ? 0 : ttl;
    }

    /**
     * 判断key是否存在
     *
     * @param key key
     * @return true-存在 false-不存在
     */
    public static boolean exist(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }


    //ZSet操作
    public static <T> boolean zAdd(String key, T value, double score) {
        return Boolean.TRUE.equals(redisTemplate.opsForZSet()
                .add(key, value, score));
    }


    public static <T> boolean zAddIfAbsent(String key, T value, double score) {
        return Boolean.TRUE.equals(redisTemplate.opsForZSet()
                .addIfAbsent(key, value, score)
        );
    }

    public static <T> Long zRem(String key, T member) {
        return redisTemplate.opsForZSet()
                .remove(key, member);
    }

    public static <T> Long zRem(String key, List<T> members) {
        return redisTemplate.opsForZSet()
                .remove(key, members.toArray());
    }

    public static <T> boolean zContains(String key, T member) {
        Double score = redisTemplate.opsForZSet()
                .score(key, member);
        return score != null;
    }

    //shit
    public static <T> Map<T, Boolean> zMContains(String key, List<T> members) {
        List<Double> scores = redisTemplate.opsForZSet()
                .score(key, members.toArray());
        if (scores == null || scores.isEmpty()) {
            return Collections.emptyMap();
        }
        HashMap<T, Boolean> map = new HashMap<>(members.size());
        for (int i = 0; i < members.size(); i++) {
            map.put(members.get(i), scores.get(i) != null);
        }
        return map;
    }

    public static <T> Long zCard(String key) {
        return redisTemplate.opsForZSet()
                .zCard(key);
    }

    public static <T> Set<ZSetOperations.TypedTuple<T>> zRevRangeWithScore(String key, long start, long end) {
        return redisTemplate.opsForZSet()
                .reverseRangeWithScores(key, start, end);
    }

    public static <T> List<T> zPage(String key, long current, long pageSize) {
        long size = Math.min(20, pageSize);
        long min = (current - 1) * pageSize;
        long max = current * pageSize - 1;
        Set set = redisTemplate.opsForZSet()
                .range(key, min, max);
        if (set == null) return Collections.emptyList();
        return new ArrayList<>(set);
    }

    public static <T> List<T> zRevPage(String key, long current, long pageSize) {
        long size = Math.min(20, pageSize);
        long min = (current - 1) * pageSize;
        long max = current * pageSize - 1;
        Set set = redisTemplate.opsForZSet()
                .reverseRange(key, min, max);
        if (set == null) return Collections.emptyList();
        return new ArrayList<>(set);
    }

    public static <T> Long zAddAll(String key, Set<ZSetOperations.TypedTuple<T>> items) {
        return redisTemplate.opsForZSet()
                .add(key, items);
    }

    /**
     * 设置Map
     *
     * @param key
     * @param value
     * @param <T>
     */
    public static <T> void hMSet(String key, Map<String, T> value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        RedisSerializer keySerializer = redisTemplate.getKeySerializer();
        final Map<byte[], byte[]> map = serializeMap(value);
        final byte[] k = keySerializer.serialize(key);
        Objects.requireNonNull(k);
        redisTemplate.execute((RedisCallback) connection -> {
            connection.hashCommands().hMSet(k, map);
            return null;
        });
    }

    /**
     * 获取Map
     *
     * @param key
     * @param <T>
     * @return
     */
    public static <T> Map<String, T> hMGetAll(String key) {
        final RedisSerializer keySerializer = redisTemplate.getKeySerializer();
        final byte[] redisKey = keySerializer.serialize(key);
        Objects.requireNonNull(redisKey);
        return (Map<String, T>) redisTemplate.execute((RedisCallback<Map<String, T>>) connection -> {
            Map<byte[], byte[]> byteMap = connection.hashCommands()
                    .hGetAll(redisKey);
            return deserializeMap(byteMap);
        });
    }

    public static <T> List<Map<String, T>> hMGetAll(List<String> keys) {
        return (List<Map<String, T>>) redisTemplate
                .executePipelined((RedisCallback<Map<String, T>>) connection -> {
                    for (String key : keys) {
                        connection.hashCommands()
                                .hGetAll(hashKeyBytes(key));
                    }
                    return null;
                }, redisTemplate.getHashValueSerializer());
    }

    public static <T> List<Map<String, T>> hMGetAll(List<String> keys, BiFunction<String, Integer, Map<String, T>> provider) {
        List<Object> list = redisTemplate
                .executePipelined((RedisCallback<Object>) connection -> {
                    for (String key : keys) {
                        connection.hashCommands()
                                .hGetAll(hashKeyBytes(key));
                    }
                    return null;
                }, redisTemplate.getHashValueSerializer());
        ArrayList<Map<String, T>> result = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            Object o = list.get(i);
            if (o instanceof Map<?, ?> map && !map.isEmpty()) {
                result.add((Map<String, T>) o);
            } else {
                Map<String, T> data = provider.apply(keys.get(i), i);
                result.add(data);
            }
        }
        return result;
    }

    public static <T> void hIncr(String key, String field, Integer cnt) {
        redisTemplate.execute((RedisCallback) connection -> {
            connection.hashCommands()
                    .hIncrBy(keyBytes(key), hashKeyBytes(field), cnt);
            return null;
        });
    }

    public static <T> void hSet(String key, String field, T value) {
        redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            return connection.hashCommands()
                    .hSet(keyBytes(key), hashKeyBytes(field), hashValueBytes(value));

        });
    }

    private static <K, V> Map<byte[], byte[]> serializeMap(Map<K, V> map) {
        RedisSerializer hashKeySerializer = redisTemplate.getHashKeySerializer();
        RedisSerializer hashValueSerializer = redisTemplate.getHashValueSerializer();
        HashMap<byte[], byte[]> serializeredMap = new HashMap<>();
        map.forEach((k, v) -> serializeredMap.put(hashKeySerializer.serialize(k), hashValueSerializer.serialize(v)));
        return serializeredMap;
    }

    private static <T> Map<String, T> deserializeMap(Map<byte[], byte[]> byteMap) {
        if (byteMap == null) return Collections.emptyMap();
        Map<String, T> map = new HashMap<>();
        final RedisSerializer hashKeySerializer = redisTemplate.getHashKeySerializer();
        final RedisSerializer<T> hashValueSerializer = redisTemplate.getHashValueSerializer();
        byteMap.forEach((k, v) -> {
            String key1 = (String) hashKeySerializer.deserialize(k);
            T value = hashValueSerializer.deserialize(v);
            map.put(key1, value);
        });
        return map;
    }

    private static <T> byte[] keyBytes(T k) {
        RedisSerializer keySerializer = redisTemplate.getKeySerializer();
        return Objects.requireNonNull(keySerializer.serialize(k));
    }

    private static <T> byte[] hashKeyBytes(T hashKey) {
        RedisSerializer hashKeySerializer = redisTemplate.getHashKeySerializer();
        return Objects.requireNonNull(hashKeySerializer.serialize(hashKey));
    }

    private static <T> byte[] hashValueBytes(T value) {
        RedisSerializer hashValueSerializer = redisTemplate.getHashValueSerializer();
        return hashValueSerializer.serialize(value);
    }

    public static <T> T excuteScript(RedisScript<T> script, List<String> keys, Object... args) {
        return (T) redisTemplate.execute(script, keys, args);
    }

    public static RedisScript<Long> loadScript(String path) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        ResourceScriptSource source = new ResourceScriptSource(new ClassPathResource(path));
        script.setScriptSource(source);
        script.setResultType(Long.class);
        return script;
    }
}
