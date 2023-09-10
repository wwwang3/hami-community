package top.wang3.hami.common;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(value = {
        "top.wang3.hami.common.component",
        "top.wang3.hami.common.config",
        "top.wang3.hami.common.canal",
})
public class HamiCommonConfig {

}
