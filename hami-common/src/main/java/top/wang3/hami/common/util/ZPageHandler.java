package top.wang3.hami.common.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ZSetOperations;
import top.wang3.hami.common.HamiFactory;
import top.wang3.hami.common.lock.LockTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * Redis zset分页工具类
 * zset保存的元素数量有限制, zcard获取的不是全部的元素数量, 所以需要提供获取元素总数的方法
 * 通过传入MP的Page对象控制分页参数
 */
public class ZPageHandler {

    public static final int DEFAULT_MAX_SIZE = 8000;


    public static <T> ZPage<T> of(String key, Page<?> page) {
        return new ZPageBuilder<T>()
                .key(key)
                .page(page);
    }

    /**
     * 同时获取member和score, List中的元素是TypedTupleTuple
     * @param key zset对应的键
     * @param page 分页对象
     * @return ZPage对象
     * @param <T> member泛型
     */
    public static <T> ZPage<ZSetOperations.TypedTuple<T>> ofScore(String key, Page<?> page) {
        return new ZPageWithScoreBuilder<T>()
                .page(page)
                .key(key);
    }

    @AllArgsConstructor
    @RequiredArgsConstructor
    public static abstract class ZPage<T> {
        // zset对应的key
        String key;
        // 分页对象, 会设置total
        Page<?> page;
        // 是否倒序查询
        boolean reversed = true;
        //总数查询方法
        Supplier<Long> countSupplier;

        // 超过zset存储的最大数量, 回源查询方法
        BiFunction<Long, Long, List<T>> source;

        /**
         * 加载缓存的方法
         */
        Supplier<List<T>> valueLoader;


        public List<T> query() {
            Long count = countSupplier.get();
            // count为0直接走人
            if (count == null || count == 0) {
                // empty
                page.setTotal(0L);
                return Collections.emptyList();
            }
            page.setTotal(count);
            long current = page.getCurrent();
            long size = page.getSize();
            if (current > page.getPages() ) {
                // 超过最大页数
                return Collections.emptyList();
            }
            List<T> items = doQuery(key, current, size);
            if (!items.isEmpty()) {
                return items;
            }
            // zset没有, 可能缓存过期或者超过了最大zset存储的数量
            long max = RedisClient.zCard(key);
            if (current * size > max && max != 0) {
                //超过最大元素数量
                //回源DB
                return source == null ? Collections.emptyList() : source.apply(current, size);
            }
            // 缓存过期, 自行在查询时排序, 因为不知道查询的是倒序还是逆序
            return load(current, size);
        }

        private List<T> load(long current, long size) {
            LockTemplate lockTemplate = HamiFactory.getLockTemplate();
            return lockTemplate.execute(key, () -> {
                // 再查一次
                List<T> items = doQuery(key, current, size);
                if (!items.isEmpty()) {
                    // 有直接返回了
                   return items;
                }
                items = valueLoader.get();
                return ListMapperHandler.subList(items, Function.identity(), current, size);
            });
        }


        private List<T> doQuery(String key, long current, long size) {
            if (reversed) {
                return handleRevQuery(key, current, size);
            }
            return handleQuery(key, current, size);
        }

        public abstract List<T> handleQuery(String key, long current, long size);

        public abstract List<T> handleRevQuery(String key, long current, long size);


        public ZPage<T> key(String key) {
            this.key = key;
            return this;
        }

        public ZPage<T> page(Page<?> page) {
            this.page = page;
            this.page.setSearchCount(false);
            return this;
        }

        public ZPage<T> reversed(boolean reversed) {
            this.reversed = reversed;
            return this;
        }

        public ZPage<T> countSupplier(Supplier<Long> supplier) {
            this.countSupplier = supplier;
            return this;
        }

        public ZPage<T> source(BiFunction<Long, Long, List<T>> func) {
            this.source = func;
            return this;
        }

        public ZPage<T> loader(Supplier<List<T>> valueLoader) {
            this.valueLoader = valueLoader;
            return this;
        }
    }

    public static class ZPageBuilder<T> extends ZPage<T> {

        @Override
        public List<T> handleQuery(String key, long current, long size) {
            return RedisClient.zPage(key, current, size);
        }

        @Override
        public List<T> handleRevQuery(String key, long current, long size) {
            return RedisClient.zRevPage(key, current, size);
        }
    }

    public static class ZPageWithScoreBuilder<T> extends ZPage<ZSetOperations.TypedTuple<T>> {

        @Override
        public List<ZSetOperations.TypedTuple<T>> handleQuery(String key, long current, long size) {
            Set<ZSetOperations.TypedTuple<T>> items = RedisClient.zPageWithScore(key, current, size);
            return new ArrayList<>(items);
        }

        @Override
        public List<ZSetOperations.TypedTuple<T>> handleRevQuery(String key, long current, long size) {
            Set<ZSetOperations.TypedTuple<T>> items = RedisClient.zRevPageWithScore(key, current, size);
            return new ArrayList<>(items);
        }
    }
}
