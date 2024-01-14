package top.wang3.hami.core.async;

import com.alibaba.ttl.TtlCallable;
import com.alibaba.ttl.TtlRunnable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;
import top.wang3.hami.common.message.email.AlarmEmailMessage;
import top.wang3.hami.core.service.mail.MailMessageHandler;

import java.util.Objects;
import java.util.concurrent.*;


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


    @Slf4j
    public static class NewThreadPolicy implements RejectedExecutionHandler {

        private final MailMessageHandler handler;

        public NewThreadPolicy(MailMessageHandler mailMessageHandler) {
            this.handler = mailMessageHandler;
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            String msg = String.format(
                    "Thread pool is EXHAUSTED!"
                            + " Pool Size: %d (active: %d, core: %d, max: %d, largest: %d),"
                            + " Task: %d (completed: %d),"
                            + " Executor status:(isShutdown:%s, isTerminated:%s, isTerminating:%s)",
                    executor.getPoolSize(),
                    executor.getActiveCount(),
                    executor.getCorePoolSize(),
                    executor.getMaximumPoolSize(),
                    executor.getLargestPoolSize(),
                    executor.getTaskCount(),
                    executor.getCompletedTaskCount(),
                    executor.isShutdown(),
                    executor.isTerminated(),
                    executor.isTerminating());
            log.error(msg);
            try {
                //try new thread
                new Thread(r, "Temporary task executor").start();
                handler.handle(new AlarmEmailMessage("线程池告警信息", msg));
            } catch (Throwable e) {
                msg = "Failed to start a new Thread, error_class: %s, error_msg: %s".formatted(e.getClass(), e.getMessage());
                handler.handle(new AlarmEmailMessage("资源告警信息", msg));
                throw new RejectedExecutionException(msg, e);
            }
        }
    }
}
