package top.wang3.hami.core.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import top.wang3.hami.core.component.TtlThreadPoolTaskExecutor;
import top.wang3.hami.core.service.mail.MailMessageHandler;

@EnableAsync
@Configuration
public class AsyncConfig {

    @Primary
    @Bean("hami-thread-pool")
    public TaskExecutor threadPoolExecutor(MailMessageHandler handler) {
        TtlThreadPoolTaskExecutor taskExecutor = new TtlThreadPoolTaskExecutor();
        taskExecutor.setBeanName("hami-thread-pool");
        taskExecutor.setThreadNamePrefix("hami-thread-");
        taskExecutor.setCorePoolSize(8);
        taskExecutor.setMaxPoolSize(256);
        taskExecutor.setQueueCapacity(1024);
        taskExecutor.setKeepAliveSeconds(60);
        taskExecutor.setPrestartAllCoreThreads(true);
        taskExecutor.initialize();
        taskExecutor.setRejectedExecutionHandler(new TtlThreadPoolTaskExecutor.NewThreadPolicy(handler));
        return taskExecutor;
    }


}
