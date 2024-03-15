package top.wang3.hami.core.lock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.wang3.hami.common.lock.LockTemplate;

@Configuration
public class LockTemplateConfig {

    @Bean
    @ConditionalOnMissingBean(LockTemplate.class)
    public LockTemplate localLockTemplate() {
        return new LocalLockTemplateSupport();
    }

}
