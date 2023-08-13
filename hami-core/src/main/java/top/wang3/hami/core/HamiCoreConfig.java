package top.wang3.hami.core;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan(basePackages = {
        "top.wang3.hami.core.config",
        "top.wang3.hami.core.service",
        "top.wang3.hami.core.handler",
        "top.wang3.hami.common.component",
        "top.wang3.hami.common.config",
})
@MapperScan(basePackages = "top.wang3.hami.core.mapper")
@Configuration
public class HamiCoreConfig {

}
