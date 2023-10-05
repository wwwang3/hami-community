package top.wang3.hami.core.component;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import top.wang3.hami.common.util.RedisClient;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class ZPageHandler {

    public static final int DEFAULT_MAX_SIZE = 8192;


    public static <T> ZPageBuilder<T> of(String key, Object lock) {
        ZPageBuilder<T> builder = new ZPageBuilder<>(lock);
        return builder.key(key);
    }

    public static <T> ZPageBuilder<T> of(String key, Page<?> page, Object lock) {
        return new ZPageBuilder<T>(lock)
                .key(key)
                .page(page);
    }


    @AllArgsConstructor
    public static class ZPageBuilder<T> {
        //zset对应的key
        private String key;
        //分页对象, 会设置total
        private Page<?> page;
        //s=是否倒序查询
        private boolean reversed = true;
        //lock
        private final Object lock;
        //总数查询方法
        private Supplier<Long> countSupplier;
        //超过zset存储的最大数量, 回源查询方法
        private BiFunction<Long, Long, List<T>> source;

        private BiFunction<Long, Long, List<T>> loader;

        public ZPageBuilder(Object lock) {
            this.lock = lock;
        }

        public List<T> query() {
            Long count = countSupplier.get();
            //count为0直接走人
            if (count == null || count == 0) {
                //empty
                page.setTotal(0L);
                return Collections.emptyList();
            }
            page.setTotal(count);
            if (!page.hasNext()) {
                //超过最大页数
                return Collections.emptyList();
            }
            //到这里说明current * size <= total, 这一页一定有数据
            long current = page.getCurrent();
            long size = page.getSize();
            //先查询Zset有没有
            List<T> items = handleQuery(current, size);
            if (!items.isEmpty()) {
                return items;
            }
            //zset没有, 可能缓存过期或者超过了最大zset存储的数量
            long max = RedisClient.zCard(key);
            if (current * size > max && max != 0) {
                //回源DB
                return source == null ? Collections.emptyList() : source.apply(count, size);
            }
            //缓存过期
            return load(current, size);
        }

        private List<T> handleQuery(long current, long size) {
            if (reversed) {
                return RedisClient.zRevPage(key, current, size);
            } else {
                return RedisClient.zPage(key, current, size);
            }
        }

        private List<T> load(long current, long size) {
            synchronized (lock) {
                List<T> items = handleQuery(current, size);
                if (!items.isEmpty()) {
                    return items;
                }
                return loader.apply(current, size);
            }
        }

        public ZPageBuilder<T> key(String key) {
            this.key = key;
            return this;
        }

        public ZPageBuilder<T> page(Page<?> page) {
            this.page = page;
            this.page.setSearchCount(false);
            return this;
        }

        public ZPageBuilder<T> reversed(boolean reversed) {
            this.reversed = reversed;
            return this;
        }

        public ZPageBuilder<T> countSupplier(Supplier<Long> supplier) {
            this.countSupplier = supplier;
            return this;
        }

        public ZPageBuilder<T> source(BiFunction<Long, Long, List<T>> func) {
            this.source = func;
            return this;
        }

        public ZPageBuilder<T> loader(BiFunction<Long, Long, List<T>> func) {
            this.loader = func;
            return this;
        }

    }
}
