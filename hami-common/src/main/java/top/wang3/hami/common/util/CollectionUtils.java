package top.wang3.hami.common.util;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class CollectionUtils {


    /**
     * 将list中的元素转换为另一种实体
     * @param origin 原来的list
     * @param mapper mapper
     * @return List<U>
     * @param <T> origin中的元素泛型
     * @param <U> 新列表中的元素类型
     */
    public static <T, U> List<U> convert(List<T> origin, Function<T, U> mapper) {
        return Optional.ofNullable(origin)
                .map(t -> t.stream().map(mapper).toList())
                .orElse(Collections.emptyList());
    }
}
