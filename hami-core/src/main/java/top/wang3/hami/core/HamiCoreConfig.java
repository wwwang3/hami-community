package top.wang3.hami.core;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import top.wang3.hami.common.HamiCommonConfig;

@ComponentScan(basePackages = {
        "top.wang3.hami.core.config",
        "top.wang3.hami.core.service",
        "top.wang3.hami.core.handler",
        "top.wang3.hami.core.component",
})
@Import(value = {HamiCommonConfig.class})
@MapperScan(basePackages = "top.wang3.hami.core.mapper")
@Configuration
public class HamiCoreConfig {

}
