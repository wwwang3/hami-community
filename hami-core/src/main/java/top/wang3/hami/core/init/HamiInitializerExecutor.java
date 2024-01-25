package top.wang3.hami.core.init;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.OrderComparator;
import org.springframework.core.task.TaskExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.util.AsyncStopWatch;
import top.wang3.hami.core.HamiProperties;
import top.wang3.hami.core.exception.HamiServiceException;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "hami.init", value = "enable", havingValue = "true")
@Slf4j
public class HamiInitializerExecutor implements ApplicationRunner, InitializingBean {

    private List<HamiInitializer> initializers;
    private List<InitializerEnums> enabledInitializers;

    private TaskExecutor taskExecutor;
    private CountDownLatch latch;

    @Autowired
    public void setEnabledInitializers(HamiProperties hamiProperties) {
        enabledInitializers = hamiProperties
                .getInit().getList();
    }

    @Autowired(required = false)
    public void setInitializers(List<HamiInitializer> initializers) {
        this.initializers = initializers;
        if (initializers != null) {
            OrderComparator.sort(initializers);
            log.info("find {} initializer", initializers.size());
        }
    }

    @Autowired(required = false)
    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (initializers == null || initializers.isEmpty()) {
            return;
        }
        log.info("start to execute initialization tasks");
        AsyncStopWatch watch = new AsyncStopWatch("hami-cache-init-task");
        processInitializingTask(watch);
        prettyPrint(watch);
    }

    private void processInitializingTask(AsyncStopWatch watch) {
        for (HamiInitializer initializer : initializers) {
            if (!enabled(initializer)) {
                continue;
            }
            if (latch != null && initializer.async()) {
                handleAsyncRun(initializer, watch);
            } else {
                handleRun(initializer, watch);
            }
        }
        try {
            // wait all task finish if it has async task
            if (latch != null) {
                latch.await();
            }
        } catch (InterruptedException e) {
            throw new HamiServiceException(e.getMessage(), e);
        }
    }

    private void handleRun(HamiInitializer initializer, AsyncStopWatch watch) {
        String id = "[task-" + initializer.getName() + "]";
        watch.start(id) ;
        initializer.run();
        watch.stop(id);
    }

    @SuppressWarnings("all")
    private void handleAsyncRun(HamiInitializer initializer, AsyncStopWatch watch) {
        String id = "[task-" + initializer.getName() + "]";
        watch.start(id);
        CompletableFuture
                .runAsync(initializer, taskExecutor)
                .whenComplete((rs, th) -> {
                    if (th != null) {
                        log.info("execute task: {} failed, error_class: {}, error_msg: {}",
                                id,
                                th.getClass().getName(),
                                th.getMessage());
                        th.printStackTrace();
                    }
                    watch.stop(id);
                    this.latch.countDown();
                });
    }

    private boolean enabled(@NonNull HamiInitializer initializer) {
        return initializer.alwaysExecute() || (enabledInitializers.contains(initializer.getName()));
    }

    void prettyPrint(@NonNull AsyncStopWatch watch) {
        long nanos = watch.getTotalTimeNanos();
        long minutes = nanos / 1_000_000_000 / 60;
        long seconds = nanos / 1_000_000_000 % 60;
        long millis = nanos % 1_000_000_000 / 1_000_000;
        long ns = nanos % 1_000_000_000 % 1_000_000;
        int count = watch.getTaskCount();
        String s = "%s task cost: %sM %sS %sMS %sNS".formatted(count, minutes, seconds, millis, ns);
        String info = watch.prettyPrint();
        log.info("\nInitializing tasks execution status: \n{}\n{}", s, info);
    }

    @Override
    public void afterPropertiesSet() {
        int size = 0;
        if (initializers != null && taskExecutor != null) {
            for (HamiInitializer initializer : initializers) {
                if (initializer.async() && enabled(initializer)) {
                    size++;
                }
            }
        }
        if (size != 0) {
            latch = new CountDownLatch(size);
        }
    }
}
