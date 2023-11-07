package top.wang3.hami.core.component;

import com.alibaba.ttl.TtlCallable;
import com.alibaba.ttl.TtlRunnable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;


@SuppressWarnings(value = {"all"})
@Slf4j
public class TtlThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {

    /**
     * 错误提示语
     */
    private static final String ERROR_MESSAGE = "task不能为空";

    @Override
    public void execute(Runnable task) {
        super.execute(Objects.requireNonNull(TtlRunnable.get(task), ERROR_MESSAGE));
    }

    @Override
    public void execute(Runnable task, long startTimeout) {
        super.execute(Objects.requireNonNull(TtlRunnable.get(task), ERROR_MESSAGE), startTimeout);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return super.submit(Objects.requireNonNull(TtlRunnable.get(task), ERROR_MESSAGE));
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return super.submit(Objects.requireNonNull(TtlCallable.get(task), ERROR_MESSAGE));
    }

    @Override
    public ListenableFuture<?> submitListenable(Runnable task) {
        return super.submitListenable(Objects.requireNonNull(TtlRunnable.get(task), ERROR_MESSAGE));
    }

    @Override
    public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
        return super.submitListenable(Objects.requireNonNull(TtlCallable.get(task), ERROR_MESSAGE));
    }

    @Override
    public void destroy() {
        super.destroy();
        log.info("hami-thread-pool shutdown success");
    }

}
