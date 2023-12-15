package top.wang3.hami.canal.annotation;


import org.springframework.context.annotation.Import;
import top.wang3.hami.canal.config.CanalRegistrarSelector;
import top.wang3.hami.canal.config.HamiCanalConfiguration;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({CanalRegistrarSelector.class, HamiCanalConfiguration.class})
public @interface EnableCanal {
}
