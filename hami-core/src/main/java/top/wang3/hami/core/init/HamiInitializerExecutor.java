package top.wang3.hami.core.init;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.OrderComparator;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import top.wang3.hami.core.HamiProperties;

import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "hami.init.enable", havingValue = "true", matchIfMissing = true)
@Slf4j
public class HamiInitializerExecutor implements ApplicationRunner {

    private final HamiProperties hamiProperties;

    private List<HamiInitializer> initializers;
    private List<String> enabledInitializers;

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

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("start to execute initialization tasks");
        if (initializers == null || initializers.isEmpty()) {
            return;
        }
        StopWatch watch = new StopWatch();
        for (HamiInitializer initializer : initializers) {
            handleRun(initializer, watch);
        }
        log.info("initializing tasks execution status");
        System.out.println(watch.prettyPrint());
    }

    private void handleRun(HamiInitializer initializer, StopWatch watch) {
        watch.start("[initializing task]-" + initializer.getName());
        if (enabledInitializers == null || enabledInitializers.isEmpty()
                || !enabledInitializers.contains(initializer.getName())) {
            return;
        }
        initializer.run();
        watch.stop();
    }
}
