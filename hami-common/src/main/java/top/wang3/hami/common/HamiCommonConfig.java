package top.wang3.hami.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import top.wang3.hami.common.lock.LockTemplate;

@Configuration
@ComponentScan(value = {
        "top.wang3.hami.common.component",
})
public class HamiCommonConfig implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        LockTemplate template = applicationContext.getBean(LockTemplate.class);
        Assert.notNull(template, "The implementation of LockTemplate cannot be found");
        HamiFactory.registerLockTemplate(template);
    }
}
