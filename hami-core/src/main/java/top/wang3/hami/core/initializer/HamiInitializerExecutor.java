package top.wang3.hami.core.initializer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.OrderComparator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "hami.init",
        value = "hami.init.enable", havingValue = "true", matchIfMissing = true)
@Slf4j
public class HamiInitializerExecutor implements ApplicationRunner {

    private List<HamiInitializer> initializers;

    @Value(value = "${hami.init.list}")
    private List<String> enabledInitializers;

    @Autowired(required = false)
    public void setInitializers(List<HamiInitializer> initializers) {
        this.initializers = initializers;
        if (initializers != null) {
            OrderComparator.sort(initializers);
            log.debug("find {} initializer", initializers.size());
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (initializers == null || initializers.isEmpty()) {
            return;
        }
        for (HamiInitializer initializer : initializers) {
            handleRun(initializer);
        }
    }

    private void handleRun(HamiInitializer initializer) {
        if (enabledInitializers == null || enabledInitializers.isEmpty()
                || !enabledInitializers.contains(initializer.getName())) {
            return;
        }
        initializer.run();
    }
}
