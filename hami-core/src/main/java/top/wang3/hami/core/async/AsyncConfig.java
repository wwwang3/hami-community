package top.wang3.hami.core.async;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import top.wang3.hami.core.service.mail.MailMessageHandler;

@EnableAsync
@Configuration
@RequiredArgsConstructor
public class AsyncConfig implements AsyncConfigurer {

    private final MailMessageHandler mailMessageHandler;

    @Primary
    @Bean("hami-thread-pool")
    public ThreadPoolTaskExecutor threadPoolExecutor() {
        TtlThreadPoolTaskExecutor taskExecutor = new TtlThreadPoolTaskExecutor();
        taskExecutor.setBeanName("hami-thread-pool");
        taskExecutor.setThreadNamePrefix("hami-thread-");
        taskExecutor.setCorePoolSize(8);
        taskExecutor.setMaxPoolSize(256);
        taskExecutor.setQueueCapacity(1024);
        taskExecutor.setKeepAliveSeconds(60);
        taskExecutor.setPrestartAllCoreThreads(true);
        taskExecutor.setRejectedExecutionHandler(new TtlThreadPoolTaskExecutor.NewThreadPolicy(mailMessageHandler));
        return taskExecutor;
    }


}
