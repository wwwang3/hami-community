package top.wang3.hami.core;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import top.wang3.hami.common.HamiCommonConfig;
import top.wang3.hami.mail.EnableMail;

@ComponentScan(basePackages = {
        "top.wang3.hami.core.config",
        "top.wang3.hami.core.repository",
        "top.wang3.hami.core.service",
        "top.wang3.hami.core.component",
        "top.wang3.hami.core.job",
        "top.wang3.hami.core.aspect",
})
@Import(value = {HamiCommonConfig.class})
@MapperScan(basePackages = "top.wang3.hami.core.mapper")
@Configuration
@EnableScheduling
@EnableMail
public class HamiCoreConfig {

}
