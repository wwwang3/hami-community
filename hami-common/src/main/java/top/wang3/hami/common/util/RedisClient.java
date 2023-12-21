package top.wang3.hami.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.zset.Tuple;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Redis工具类
 */
@SuppressWarnings(value = {"all"})
@Slf4j
public class RedisClient {

    private static RedisTemplate redisTemplate;

    public static void register(RedisTemplate template) {
        redisTemplate = template;
    }

    public static RedisTemplate getTemplate() {
        return redisTemplate;
    }

    public static boolean simpleLock(String key, long timeout, TimeUnit timeUnit) {
        return setNx(key, UUID.randomUUID().toString(), timeout, timeUnit);
    }

    public static boolean unLock(String key) {
        return deleteObject(key);
    }

    public static Long incrBy(String key, long delta) {
        return redisTemplate.opsForValue()
                .increment(key, delta);
    }

    public static Long incr(String key) {
        return redisTemplate.opsForValue()
                .increment(key);
    }

    public static Long decr(String key) {
        return redisTemplate.opsForValue()
                .decrement(key);
    }

    public static <T> void cacheEmptyObject(String key, Object o) {
        RedisClient.setCacheObject(key, o, 1, TimeUnit.MINUTES);
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
        redisTemplate.opsForValue().set(key, value, timeout + randomSeconds(), TimeUnit.SECONDS);
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
        redisTemplate.opsForValue().set(key, value, timeout + randomSeconds(), timeUnit);
    }

    public static <T> void cacheMultiObject(final Map<String, T> data) {
        redisTemplate.opsForValue()
                .multiSet(data); //no expire
    }

    public static <T> void cacheMultiObject(List<T> items, Function<T, String> keyMapper, long min, long max, TimeUnit timeUnit) {
        Objects.requireNonNull(keyMapper);
        Map<String, T> map = ListMapperHandler.listToMap(items, keyMapper);
        cacheMultiObject(map, min, max, timeUnit);
    }

    public static <T> void cacheMultiObject(final Map<String, T> data, final long min, long max, final TimeUnit timeUnit) {
        Assert.notEmpty(data, "map can not be empty");
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            Map<byte[], byte[]> map = serializeMap(data);
            connection
                    .stringCommands()
                    .mSet(map);
            data.keySet().forEach(key -> {
                byte[] rawKey = keyBytes(key);
                connection.
                        keyCommands()
                        .pExpire(rawKey, timeUnit.toMillis(RandomUtils.randomLong(min, max)) + randomMills());
            });
            return null;
        });
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

    public static boolean pExpire(final String key, final long timeout) {
        return expire(key, timeout, TimeUnit.MILLISECONDS);
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
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout + randomMills(), unit));
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

    public static <T> List<T> getMultiCacheObject(final Collection<String> keys) {
        Assert.notNull(keys, "keys cannot be null");
        return redisTemplate.opsForValue()
                .multiGet(keys);
    }

    public static <T> List<T> getMultiCacheObject(final List<String> keys, BiFunction<String, Integer, T> func) {
        List<T> data = redisTemplate.opsForValue()
                .multiGet(keys);
        if (data == null || data.isEmpty()) return Collections.emptyList();
        ListMapperHandler.forEach(data, (item, i) -> {
            if (item == null) {
                T applied = func.apply(keys.get(i), i);
                data.set(i, applied);
            }
        });
        return data;
    }

    public static <K, T> List<T> getMultiCacheObject(String keyPrefix,
                                                     Collection<K> keyItems,
                                                     Function<List<K>, Collection<T>> func) {
        List<K> items = keyItems.stream().distinct().toList();
        //already distinct
        Collection<String> keys = ListMapperHandler.listTo(items, item -> keyPrefix + item, false);
        List<T> data = redisTemplate.opsForValue()
                .multiGet(keys);
        if (data == null || data.isEmpty()) return Collections.emptyList();
        ArrayList<T> results = new ArrayList<>(keyItems.size());
        ArrayList<K> nullKeys = new ArrayList<>();
        ListMapperHandler.forEach(data, (value, index) -> {
            if (value == null) {
                nullKeys.add(items.get(index));
            } else {
                results.add(value);
            }
        });
        if (nullKeys.isEmpty() || func == null) {
            return results;
        }
        Collection<T> absentValues = func.apply(nullKeys);
        results.addAll(absentValues);
        return results;
    }

    public static <K, V> Map<K, V> getMultiCacheObjectToMap(String keyPrefix,
                                                            Collection<K> keyItems,
                                                            Function<List<K>, Map<K, V>> func) {
        List<K> items = keyItems.stream().distinct().toList();
        //already distinct
        Collection<String> keys = ListMapperHandler.listTo(items, item -> keyPrefix + item);
        List<V> data = redisTemplate.opsForValue()
                .multiGet(keys);
        if (data == null || data.isEmpty()) return Collections.emptyMap();
        HashMap<K, V> map = new HashMap<>(keyItems.size());
        ArrayList<K> nullKeys = new ArrayList<>();
        ListMapperHandler.forEach(data, (value, index) -> {
            K key = items.get(index);
            if (value != null) {
                map.put(key, value);
            } else {
                nullKeys.add(key);
            }
        });
        if (nullKeys.isEmpty() || func == null) {
            return map;
        }
        Map<K, V> absentValues = func.apply(nullKeys);
        map.putAll(absentValues);
        return map;
    }

    /**
     * 获取多个缓存对象 collection建议传入ArrayList
     *
     * @param keys keys
     * @param func 当获取到的缓存对象为null时. 调用的方法，其中List中为null元素的索引
     * @param <T>
     * @return
     */
    public static <T> List<T> getMultiCacheObject(final Collection<String> keys, Function<List<Integer>, List<T>> func) {
        List<T> data = redisTemplate.opsForValue()
                .multiGet(keys);
        if (CollectionUtils.isEmpty(data)) return Collections.emptyList();
        int size = data.size();
        final ArrayList<T> result = new ArrayList<>(size);
        final ArrayList<Integer> list = new ArrayList<>(size);
        ListMapperHandler.forEach(data, (item, index) -> {
            if (item == null) list.add(index);
            else result.add(item);
        });
        if (!list.isEmpty()) {
            List<T> applied = func.apply(list);
            if (applied != null) {
                result.addAll(applied);
            }
        }
        return result;
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
     * 返回-1 表示key存在但是没有关联超时时间返回 -1
     */
    public static long getExpire(String key) {
        Long ttl = redisTemplate.getExpire(key);
        return ttl == null ? -2 : ttl;
    }

    /**
     * 以毫秒为单位获取密钥的生存时间。
     *
     * @param key 缓存对象的key
     * @return 剩余的过期时间 单位为秒
     * redisTemplate.getExpire
     * 返回-2 表示key不存在
     * 返回-1 表示key存在但是没有关联超时时间返回 -1
     */
    public static long getPExpire(String key) {
        Long expire = redisTemplate.getExpire(key, TimeUnit.MICROSECONDS);
        return expire == null ? -2 : expire;
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
        if (CollectionUtils.isEmpty(members)) {
            return Collections.emptyMap();
        }
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

    public static <T> long zCard(String key) {
        Long count = redisTemplate.opsForZSet()
                .zCard(key);
        return count == null ? 0L : count;
    }

    public static <T> Set<ZSetOperations.TypedTuple<T>> zRevRangeWithScore(String key, long start, long end) {
        return redisTemplate.opsForZSet()
                .reverseRangeWithScores(key, start, end);
    }

    public static <T> List<T> zPage(String key, long current, long size) {
        size = Math.min(20, size);
        long min = (current - 1) * size;
        long max = current * size - 1;
        Set set = redisTemplate.opsForZSet()
                .range(key, min, max);
        if (set == null) return Collections.emptyList();
        return new ArrayList<>(set);
    }

    public static <T> List<T> zRevPage(String key, long current, long size) {
        size = Math.min(20, size);
        long min = (current - 1) * size;
        long max = current * size - 1;
        Set<T> set = redisTemplate.opsForZSet()
                .reverseRange(key, min, max);
        if (set == null) return Collections.emptyList();
        return new ArrayList<>(set);
    }

    public static <T> Collection<ZSetOperations.TypedTuple<T>> zRevPageWithScore(String key, long current, long size) {
        size = Math.min(20, size);
        long min = (current - 1) * size;
        long max = current * size - 1;
        return redisTemplate.opsForZSet()
                .reverseRangeWithScores(key, min, max);
    }

    public static <T> Collection<ZSetOperations.TypedTuple<T>> zPageWithScore(String key, long current, long size) {
        size = Math.min(20, size);
        long min = (current - 1) * size;
        long max = current * size - 1;
        return redisTemplate.opsForZSet()
                .rangeWithScores(key, min, max);
    }

    /**
     * 不能缓存空列表
     *
     * @param key
     * @param items
     * @param <T>
     * @return
     */
    public static <T> Long zAddAll(String key, Set<ZSetOperations.TypedTuple<T>> items) {
        return redisTemplate.opsForZSet()
                .add(key, items);
    }

    public static <T> void zSetAll(String key, Collection<? extends Tuple> items, long timeout, TimeUnit timeUnit) {
        final byte[] rawKey = keyBytes(key);
        List<? extends List<? extends Tuple>> lists = ListMapperHandler.split(items, 1000);

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            connection.keyCommands()
                    .del(rawKey);
            for (List<? extends Tuple> list : lists) {
                connection.zAdd(rawKey, new LinkedHashSet<>(list));
            }
            connection.keyCommands()
                    .pExpire(rawKey, timeUnit.toMillis(timeout) + randomMills());
            return null;
        });
    }

    public static Double zIncr(String key, String member, int incr) {
        return redisTemplate.opsForZSet()
                .incrementScore(key, member, incr);
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

    public static <T> void hMSet(String key, Map<String, T> data, long timeout, TimeUnit timeUnit) {
        Objects.requireNonNull(data);
        final byte[] keyBytes = keyBytes(key);
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            connection.hashCommands()
                    .hMSet(keyBytes, serializeMap(data));
            connection.keyCommands()
                    .pExpire(keyBytes, timeUnit.toMillis(timeout) + randomMills());
            return null;
        });
    }

    /**
     * 使用Pipeline 批量写入Map, 不建议元素太多，推荐500个
     *
     * @param data
     * @param <T>
     */
    public static <T> void hMSet(Map<String, Map<String, T>> data) {
        Objects.requireNonNull(data);
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            data.forEach((key, value) -> {
                connection.hashCommands()
                        .hMSet(keyBytes(key), serializeMap(value));
            });
            return null;
        });
    }

    public static <T> void hMSet(Map<String, Map<String, T>> data, final long timeout, final TimeUnit unit) {
        Objects.requireNonNull(data);
        Objects.requireNonNull(unit);
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            data.forEach((key, value) -> {
                byte[] rawKey = keyBytes(key);
                connection.hashCommands()
                        .hMSet(rawKey, serializeMap(value));
                connection.keyCommands()
                        .pExpire(rawKey, unit.toMillis(timeout) + randomMills());
            });
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

    public static <T> void hIncr(String key, String field, long cnt) {
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
        Map<byte[], byte[]> serializeredMap = new LinkedHashMap<>(map.size());
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

    public static <T> byte[] keyBytes(T k) {
        RedisSerializer keySerializer = redisTemplate.getKeySerializer();
        return Objects.requireNonNull(keySerializer.serialize(k));
    }

    public static <T> byte[] valueBytes(T value) {
        return redisTemplate.getValueSerializer().serialize(value);
    }

    public static <T> byte[] hashKeyBytes(T hashKey) {
        RedisSerializer hashKeySerializer = redisTemplate.getHashKeySerializer();
        return Objects.requireNonNull(hashKeySerializer.serialize(hashKey));
    }

    public static <T> byte[] hashValueBytes(T value) {
        RedisSerializer hashValueSerializer = redisTemplate.getHashValueSerializer();
        return hashValueSerializer.serialize(value);
    }

    public static RedisScript<Long> loadScript(String path) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        ResourceScriptSource source = new ResourceScriptSource(new ClassPathResource(path));
        script.setScriptSource(source);
        script.setResultType(Long.class);
        log.debug("load lua script: {} success", path);
        return script;
    }


    public static <T> RedisScript<List<T>> loadScript(String path, Class<T> clazz) {
        DefaultRedisScript script = new DefaultRedisScript<>();
        ResourceScriptSource source = new ResourceScriptSource(new ClassPathResource(path));
        script.setScriptSource(source);
        script.setResultType(List.class);
        log.debug("load lua script: {} success", path);
        return (DefaultRedisScript<List<T>>) script;
    }

    public static <T> T executeScript(RedisScript<T> script, List<String> keys, List<?> args) {
        Collection<String> stringArgs = ListMapperHandler.listTo(args, String::valueOf, false);
        Object[] argsArray = stringArgs.toArray(new String[0]);
        return (T) redisTemplate.execute(script, redisTemplate.getStringSerializer(),
                redisTemplate.getValueSerializer(), keys, argsArray);
    }

    private static long randomSeconds() {
        return RandomUtils.randomLong(33, 222);
    }

    private static long randomMills() {
        return RandomUtils.randomLong(22, 333) * 1000;
    }

}
