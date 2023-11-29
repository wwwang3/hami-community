package top.wang3.hami.core.component;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ZSetOperations;
import top.wang3.hami.common.util.RedisClient;

import java.util.Collection;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class ZPageHandler {

    public static final int DEFAULT_MAX_SIZE = 8000;

    public static <T> ZPage<T> of(String key, Object lock) {
        return new ZPageBuilder<T>(lock)
                .key(key);
    }

    public static <T> ZPage<T> of(String key, Page<?> page, Object lock) {
        return new ZPageBuilder<T>(lock)
                .key(key)
                .page(page);
    }

    public static <T> ZPage<ZSetOperations.TypedTuple<T>> ofScore(String key, Object lock) {
        return new ZPageWithScoreBuilder<T>(lock)
                .key(key);
    }

    public static <T> ZPage<ZSetOperations.TypedTuple<T>> ofScore(String key, Page<?> page, Object lock) {
        return new ZPageWithScoreBuilder<T>(lock)
                .page(page)
                .key(key);
    }

    @AllArgsConstructor
    @RequiredArgsConstructor
    public static abstract class ZPage<T> {
        //zset对应的key
        String key;
        //分页对象, 会设置total
        Page<?> page;
        //s=是否倒序查询
        boolean reversed = true;
        //lock
        final Object lock;
        //总数查询方法
        Supplier<Long> countSupplier;

        //超过zset存储的最大数量, 回源查询方法
        BiFunction<Long, Long, Collection<T>> source;

        BiFunction<Long, Long, Collection<T>> loader;


        public Collection<T> query() {
            Long count = countSupplier.get();
            //count为0直接走人
            if (count == null || count == 0) {
                //empty
                page.setTotal(0L);
                return Collections.emptyList();
            }
            page.setTotal(count);
            long current = page.getCurrent();
            long size = page.getSize();
            if (current > page.getPages() ) {
                //超过最大页数
                return Collections.emptyList();
            }
            Collection<T> items = query(key, current, size);
            if (!items.isEmpty()) {
                return items;
            }
            //zset没有, 可能缓存过期或者超过了最大zset存储的数量
            long max = RedisClient.zCard(key);
            if (current * size > max && max != 0) {
                //超过最大元素数量
                //回源DB
                return source == null ? Collections.emptyList() : source.apply(current, size);
            }
            //缓存过期
            return load(current, size);
        }

        private Collection<T> load(long current, long size) {
            synchronized (lock) {
                Collection<T> items = handleQuery(key, current, size);
                if (!items.isEmpty()) {
                    return items;
                }
                return loader.apply(current, size);
            }
        }

        private Collection<T> query(String key, long current, long size) {
            if (reversed) {
                return handleRevQuery(key, current, size);
            }
            return handleQuery(key, current, size);
        }

        public abstract Collection<T> handleQuery(String key, long current, long size);

        public abstract Collection<T> handleRevQuery(String key, long current, long size);


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

        public ZPage<T> source(BiFunction<Long, Long, Collection<T>> func) {
            this.source = func;
            return this;
        }

        public ZPage<T> loader(BiFunction<Long, Long, Collection<T>> func) {
            this.loader = func;
            return this;
        }
    }

    public static class ZPageBuilder<T> extends ZPage<T> {

        public ZPageBuilder(Object lock) {
            super(lock);
        }

        @Override
        public Collection<T> handleQuery(String key, long current, long size) {
            return RedisClient.zPage(key, current, size);
        }

        @Override
        public Collection<T> handleRevQuery(String key, long current, long size) {
            return RedisClient.zRevPage(key, current, size);
        }
    }

    public static class ZPageWithScoreBuilder<T> extends ZPage<ZSetOperations.TypedTuple<T>> {

        public ZPageWithScoreBuilder(Object lock) {
            super(lock);
        }

        @Override
        public Collection<ZSetOperations.TypedTuple<T>> handleQuery(String key, long current, long size) {
            return RedisClient.zPageWithScore(key, current, size);
        }

        @Override
        public Collection<ZSetOperations.TypedTuple<T>> handleRevQuery(String key, long current, long size) {
            return RedisClient.zRevPageWithScore(key, current, size);
        }
    }
}
