package top.wang3.hami.core.cache;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public interface CacheService {

    <T> T get(String key, Supplier<T> loader);

    <T> T get(String key, Supplier<T> loader, long timeout, TimeUnit timeUnit);

    <T, R> List<R> multiGet(String keyPrefix, List<T> items, Function<T, R> loader, long timeout, TimeUnit timeUnit);

    boolean expireAndIncrBy(String key, int delta, long timeout, TimeUnit timeUnit);

}
