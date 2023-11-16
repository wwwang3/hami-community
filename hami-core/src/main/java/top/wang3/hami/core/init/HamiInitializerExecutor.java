package top.wang3.hami.core.init;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.OrderComparator;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import top.wang3.hami.core.HamiProperties;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class HamiInitializerExecutor implements ApplicationRunner {

    private List<HamiInitializer> initializers;
    private List<InitializerEnums> enabledInitializers;
    private boolean enable;

    @Autowired
    public void setEnabledInitializers(HamiProperties hamiProperties) {
        enabledInitializers = hamiProperties
                .getInit().getList();
        enable = hamiProperties.getInit().isEnable();
    }

    @Autowired(required = false)
    public void setInitializers(List<HamiInitializer> initializers) {
        this.initializers = initializers;
        if (initializers != null) {
            OrderComparator.sort(initializers);
            log.info("find {} initializer", initializers.size());
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (initializers == null || initializers.isEmpty()) {
            return;
        }
        log.info("start to execute initialization tasks");
        StopWatch watch = new StopWatch("hami-cache-init-task");
        processInitializingTask(watch);
        log.info("\nInitializing tasks execution status");
        prettyPrint(watch);
    }

    private void processInitializingTask(StopWatch watch) {
        for (HamiInitializer initializer : initializers) {
            if (!canRun(initializer)) {
                continue;
            }
            handleRun(initializer, watch);
        }
    }

    private void handleRun(HamiInitializer initializer, StopWatch watch) {
        watch.start("[task-" + initializer.getName() + "]") ;
        initializer.run();
        watch.stop();
    }

    private boolean canRun(HamiInitializer initializer) {
        return initializer.alwaysExecute() || (enable && enabledInitializers.contains(initializer.getName()));
    }

    void prettyPrint(StopWatch watch) {
        long nanos = watch.getTotalTimeNanos();
        long minutes = nanos / 1_000_000_000 / 60;
        long seconds = nanos / 1_000_000_000 % 60;
        long millis = nanos % 1_000_000_000 / 1_000_000;
        long ns = nanos % 1_000_000_000 % 1_000_000;
        int count = watch.getTaskCount();
        String s = "%s task cost: %sM %sS %sMS %sNS".formatted(count, minutes, seconds, millis, ns);
        System.out.println(s);
        System.out.println(watch.prettyPrint());
    }

}
