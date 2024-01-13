package top.wang3.hami.core.service.interact;

import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import top.wang3.hami.common.util.RandomUtils;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.exception.HamiServiceException;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class InteractHandler {

    public static RedisScript<Long> script;

    public static ThreadPoolTaskExecutor executor;

    public static long DEFAULT_EXPIRE = TimeUnit.DAYS.toMillis(30);

    public void setExecutor(ThreadPoolTaskExecutor taskExecutor) {
        executor = taskExecutor;
    }

    static {
        script = RedisClient.loadScript("/META-INF/scripts/interact.lua");
    }

    public static <T> boolean handleAction(final Interact<T> interact,
                                           Supplier<Set<ZSetOperations.TypedTuple<T>>> loader,
                                           final Consumer<Interact<T>> postAction) {
        T member = interact.member();
        if (member == null) throw new HamiServiceException("参数错误");
        long timeout = DEFAULT_EXPIRE + RandomUtils.randomLong(100, 1000);
        String key = interact.key();
        long result = execute(interact, timeout);
        if (result == -1) {
            // 过期
            String simpleKey = "interact:" + key;
            try {
                if (RedisClient.simpleLock(simpleKey, 3, TimeUnit.SECONDS)) {
                    // 加锁成功
                    Set<ZSetOperations.TypedTuple<T>> items = loader.get();
                    RedisClient.zAddAll(key, items);
                } else {
                    // 加锁失败, 直接让用户尝试下一次
                    return false;
                }
                // 加载成功
                result = execute(interact, timeout);
            } finally {
                RedisClient.simpleUnLock(simpleKey);
            }
        }
        if (result == 1) {
            //异步执行
            executor.submit(() -> {
                postAction.accept(interact);
            });
            return true;
        }
        return false;
    }


    private static <T> long execute(Interact<T> interact, long timeout) {
        List<String> args = List.of(
                String.valueOf(timeout),
                String.valueOf(interact.member()),
                String.valueOf(interact.score()),
                String.valueOf(interact.state() ? 1 : 0));
        Long res = RedisClient.executeScript(script, List.of(interact.key()), args);
        return res == null ? 0L : res;
    }

}
