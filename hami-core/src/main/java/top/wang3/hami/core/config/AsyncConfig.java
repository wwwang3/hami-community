package top.wang3.hami.core.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import top.wang3.hami.core.component.TtlThreadPoolTaskExecutor;

@EnableAsync
@Configuration
public class AsyncConfig {

    @Primary
    @Bean("hami-thread-pool")
    public TaskExecutor threadPoolExecutor() {
        TtlThreadPoolTaskExecutor taskExecutor = new TtlThreadPoolTaskExecutor();
        taskExecutor.setBeanName("hami-thread-pool");
        taskExecutor.setThreadNamePrefix("hami-thread-");
        taskExecutor.setCorePoolSize(8);
        taskExecutor.setMaxPoolSize(256);
        taskExecutor.setQueueCapacity(1024);
        taskExecutor.setKeepAliveSeconds(60);
        taskExecutor.setPrestartAllCoreThreads(true);
        taskExecutor.initialize();
        return taskExecutor;
    }


}
