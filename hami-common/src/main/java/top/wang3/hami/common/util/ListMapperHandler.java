package top.wang3.hami.common.util;

import org.springframework.data.redis.connection.zset.DefaultTuple;
import org.springframework.data.redis.connection.zset.Tuple;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 简单集合映射工具类
 */
public class ListMapperHandler {

    public static <T> List<T> subList(List<T> origin, int current, int size) {
        return subList(origin, Function.identity(), current, size);
    }


    public static <T> List<List<T>> split(Collection<T> origin, int size) {
        final List<List<T>> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(origin)) {
            return result;
        }
        ArrayList<T> subList = new ArrayList<>(size);
        for (T t : origin) {
            if (subList.size() >= size) {
                result.add(subList);
                subList = new ArrayList<>(size);
            }
            subList.add(t);
        }
        result.add(subList);
        return result;
    }

    public static <T> void forEach(Collection<T> origin, BiConsumer<T, Integer> consumer) {
        if (CollectionUtils.isEmpty(origin)) {
            return;
        }
        Iterator<T> iterator = origin.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            consumer.accept(iterator.next(), index);
            ++index;
        }
    }

    public static <T, R> List<R> subList(List<T> origin, Function<T, R> mapper, int current, int size) {
        if (CollectionUtils.isEmpty(origin)) {
            return Collections.emptyList();
        }
        if (current < 0) return Collections.emptyList();
        int length = origin.size();
        int from = (current - 1) * size;
        int to = Math.min(current * size, length);
        if (from >= length) {
            return Collections.emptyList();
        }
        List<T> list = origin.subList(from, to);
        return listTo(list, mapper);
    }

    public static <T, R> List<R> subList(List<T> origin, Function<T, R> mapper, long current, long size) {
        return subList(origin, mapper, (int) current, (int) size);
    }

    public static <T, R> List<R> listTo(Collection<T> origin, BiFunction<T, Integer, R> mapper) {
        if (origin == null || origin.isEmpty()) return Collections.emptyList();
        ArrayList<R> rs = new ArrayList<>(origin.size());
        Iterator<T> iterator = origin.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            T value = iterator.next();
            R applied = mapper.apply(value, index);
            rs.add(applied);
            ++index;
        }
        return rs;
    }

    public static <T> List<Tuple> listToTuple(List<T> origin, Function<T, Object> memberMapper, Function<T, Number> scoreMapper) {
        return listTo(origin, item -> {
            Object member = memberMapper.apply(item);
            byte[] value = RedisClient.valueBytes(member);
            Double score = scoreMapper.apply(item).doubleValue();
            return new DefaultTuple(value, score);
        });
    }

    /**
     * 将List中的元素转换为另一种元素
     *
     * @param origin origin
     * @param mapper mapper
     * @param <T>    list元素泛型
     * @param <R>    返回列表元素泛型
     * @return List<R>
     */
    public static <T, R> List<R> listTo(Collection<T> origin, Function<T, R> mapper) {
        //默认去重
        return listTo(origin, mapper, true);
    }

    public static <T, R> List<R> listTo(Collection<T> origin, Function<T, R> mapper, boolean distinct) {
        if (CollectionUtils.isEmpty(origin)) {
            return Collections.emptyList();
        }
        Stream<T> stream = origin.stream();
        if (distinct) {
            stream = stream.distinct();
        }
        return stream
                .filter(Objects::nonNull)
                .map(mapper)
                .collect(Collectors.toList());
    }

    public static <T, R> Set<R> listToSet(List<T> origin, Function<T, R> mapper) {
        return Optional.ofNullable(origin)
                .map(o -> o.stream()
                        .filter(Objects::nonNull)
                        .map(mapper)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }

    public static <T, R> Set<ZSetOperations.TypedTuple<R>> listToZSet(List<T> data, Function<T, R> memberMapper,
                                                                      Function<T, Double> scoreMapper) {
        return listToSet(data, item -> {
            R member = memberMapper.apply(item);
            Double score = scoreMapper.apply(item);
            return new DefaultTypedTuple<R>(member, score);
        });
    }

    public static <T, K> Map<K, T> listToMap(List<T> origin, Function<T, K> keyMapper) {
        if (CollectionUtils.isEmpty(origin)) {
            return Collections.emptyMap();
        }
        return listToMap(origin, keyMapper, t -> t);
    }

    public static <T, K, U> Map<K, U> listToMap(List<T> data, Function<T, K> keyMapper,
                                                Function<T, U> valueMapper) {
        if (CollectionUtils.isEmpty(data)) {
            return Collections.emptyMap();
        }
        LinkedHashMap<K, U> map = new LinkedHashMap<>(data.size());
        for (T item : data) {
            map.put(keyMapper.apply(item), valueMapper.apply(item));
        }
        return map;
    }

    /**
     * 将两个List的对象一一对应，后一个List的元素(或者其字段)是前一个List元素的字段
     * 装配 将List<U> assemble ----> Map<R, U> map
     * 确保R对应的值唯一
     * data: List<Article> assemble: List<User> ===>
     * doAssemble(data,
     * (article) -> article.getUserId, ===> userId
     * assemble,
     * (user) -> user.getUserId, ===> Map<userId, user>
     * (article, user) -> article.setUserInfo(user)
     * )
     *
     * @param data     即将装配的对象
     * @param getter   从T中获取某个字段的值
     * @param assemble 装配对象
     * @param mapKey   装配对象某个字段的值
     * @param setter   设置
     * @param <T>      待装配对象泛型
     * @param <R>      待装配对象的某个字段的泛型
     * @param <U>      装配对象泛型
     */
    public static <T, R, U> void doAssemble(List<T> data, Function<T, R> getter, List<U> assemble,
                                            Function<U, R> mapKey, BiConsumer<T, U> setter) {
        if (CollectionUtils.isEmpty(data) || CollectionUtils.isEmpty(assemble)) {
            //有一个为空直接返回
            return;
        }
        Map<R, U> map = listToMap(assemble, mapKey);
        doAssemble(data, getter, map, setter);
    }

    public static <T, R, U> void doAssemble(List<T> data, Function<T, R> getter, Map<R, U> assemble,
                                            BiConsumer<T, U> setter) {
        if (CollectionUtils.isEmpty(data) || CollectionUtils.isEmpty(assemble)) {
            return;
        }
        data.forEach(d -> {
            //忽略null
            U value = assemble.get(getter.apply(d));
            if (value != null && d != null) {
                setter.accept(d, value);
            }
        });
    }
}
