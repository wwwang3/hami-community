package top.wang3.hami.common.util;

import org.springframework.data.redis.connection.zset.Tuple;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class InteractHandler {

    public static RedisScript<Long> script;
    public static long DEFAULT_EXPIRE = TimeUnit.DAYS.toMillis(30);

    static {
        script = RedisClient.loadScript("/META-INF/scripts/interact.lua");
    }

    @SuppressWarnings("unchecked")
    public static <T> InteractBuilder<T> of(String opt) {
        return (InteractBuilder<T>) new InteractHandler()
                .createInteract()
                .opt(opt);
    }

    public <T> InteractBuilder<T> createInteract() {
        return new InteractBuilder<>();
    }

    public <T> boolean execute(InteractBuilder<T> interact) {
        interact.preCheck.accept(interact.member);
        int result = executeScript(interact);
        boolean success = handleResult(result, interact);
        Runnable postAct = interact.postAct;
        if (success && postAct != null) {
            postAct.run();
        }
        return false;
    }

    private <T> boolean handleResult(int result, InteractBuilder<T> interact) {
        return switch (result) {
            case -1 -> {
                // 过期
                String simpleKey = "interact:" + interact.key;
                try {
                    if (RedisClient.simpleLock(simpleKey, 3, TimeUnit.SECONDS)) {
                        // 加锁成功
                        Collection<Tuple> tuples = interact.loader.get();
                        RedisClient.zSetAll(interact.key, tuples, interact.timeout, interact.timeUnit);
                        // 执行lua脚本时会刷新缓存时间, 防止前面写入缓存失败
                        yield executeScript(interact) == 1;
                    } else {
                        // 加锁失败, 直接让用户尝试下一次
                        yield false;
                    }
                } finally {
                    RedisClient.unLock(simpleKey);
                }
            }
            case 1 -> true;
            case 2 -> throw new IllegalStateException("重复" + interact.opt);
            case 3 -> throw new IllegalStateException("当前用户未" + interact.opt + "过");
            default -> false;
        };
    }

    private <T> int executeScript(InteractBuilder<T> interact) {
        List<String> args = List.of(
                String.valueOf(interact.timeUnit.toMillis(interact.timeout)),
                String.valueOf(interact.member),
                String.valueOf(interact.score),
                String.valueOf(interact.state ? 1 : 0)
        );
        Long result = RedisClient.executeScript(InteractHandler.script, List.of(interact.key), args);
        return result == null ? 0 : result.intValue();
    }


    public class InteractBuilder<T> {

        String key;

        /**
         * 操作名称
         */
        String opt;

        T member;
        double score;
        boolean state;
        long timeout = InteractHandler.DEFAULT_EXPIRE;
        TimeUnit timeUnit = TimeUnit.MILLISECONDS;

        Consumer<T> preCheck = i -> {};

        Supplier<Collection<Tuple>> loader;
        Runnable postAct;

        public InteractBuilder<T> ofAction(String key, T member) {
            this.key = key;
            this.member = Objects.requireNonNull(member, "参数错误");
            this.state = true;
            this.score = new Date().getTime();
            return this;
        }

        public InteractBuilder<T>  ofCancelAction(String key, T member) {
            this.key = key;
            this.member = Objects.requireNonNull(member, "参数错误");
            this.state = false;
            this.score = new Date().getTime();
            return this;
        }

        public InteractBuilder<T> opt(String opt) {
            this.opt = StringUtils.hasText(opt) ? opt : "操作";
            return this;
        }

        public InteractBuilder<T> preCheck(Consumer<T> check) {
            this.preCheck = check;
            return this;
        }

        public InteractBuilder<T> loader(Supplier<Collection<Tuple>> loader) {
            this.loader = loader;
            return this;
        }

        public InteractBuilder<T> postAct(Runnable postAct) {
            this.postAct = postAct;
            return this;
        }

        public InteractBuilder<T> timeout(long timeout, TimeUnit timeUnit) {
            this.timeout = timeout;
            this.timeUnit = timeUnit;
            return this;
        }

        public boolean execute() {
            return InteractHandler.this.execute(this);
        }

    }
}
