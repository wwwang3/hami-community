package top.wang3.hami.common.util;

import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 简单集合映射工具类
 */
public class ListMapperHandler {

    /**
     * 将List中的元素转换为另一种元素
     * @param origin origin
     * @param mapper mapper
     * @return List<R>
     * @param <T> list元素泛型
     * @param <R> 返回列表元素泛型
     */
    public static <T, R> List<R> listTo(List<T> origin, Function<T, R> mapper) {
        return Optional.ofNullable(origin)
                .map(o -> o.stream().map(mapper).toList())
                .orElse(Collections.emptyList());
    }

    public static <T, K> Map<K, T> listToMap(List<T> origin, Function<? super T, K> keyMapper) {
        return Optional.ofNullable(origin)
                .map(o -> o.stream().collect(Collectors.toMap(keyMapper, Function.identity())))
                .orElse(Collections.emptyMap());
    }

    public static <T, K, U> Map<K, U> listToMap(List<T> data,
                                                                    Function<T, K> keyMapper,
                                                                    Function<T, U> valueMapper) {
        return Optional.ofNullable(data)
                .map(d -> d.stream().collect(Collectors.toMap(keyMapper, valueMapper)))
                .orElse(Collections.emptyMap());
    }

    /**
     * 将两个List的对象一一对应，后一个List的元素(或者其字段)是前一个List元素的字段
     * 装配 将List<U> assemble ----> Map<R, U> map
     * 确保R对应的值唯一
     * data: List<Article> assemble: List<User> ===>
     *   doAssemble(data,
     *      (article) -> article.getUserId, ===> userId
     *      assemble,
     *      (user) -> user.getUserId, ===> Map<userId, user>
     *      (article, user) -> article.setUserInfo(user)
     * )
     * @param data 即将装配的对象
     * @param getter 从T中获取某个字段的值
     * @param assemble 装配对象
     * @param mapKey 装配对象某个字段的值
     * @param setter 设置
     * @param <T> 待装配对象泛型
     * @param <R> 待装配对象的某个字段的泛型
     * @param <U> 装配对象泛型
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
            if (value != null) {
                setter.accept(d, value);
            }
        });
    }
}