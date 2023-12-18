package top.wang3.hami.canal.annotation;


import org.springframework.context.annotation.Import;
import top.wang3.hami.canal.config.CanalConfiguration;
import top.wang3.hami.canal.config.CanalListenerConfigurer;
import top.wang3.hami.canal.config.CanalRegistrarSelector;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({CanalRegistrarSelector.class, CanalConfiguration.class, CanalListenerConfigurer.class})
public @interface EnableCanal {
}
