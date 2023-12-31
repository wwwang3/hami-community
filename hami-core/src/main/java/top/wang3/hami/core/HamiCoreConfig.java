package top.wang3.hami.core;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import top.wang3.hami.common.HamiCommonConfig;


@MapperScan(basePackages = "top.wang3.hami.core.mapper")
@Configuration
@EnableScheduling
@EnableConfigurationProperties(HamiProperties.class)
@Import(value = {HamiCommonConfig.class, CoreConfigurer.class})
public class HamiCoreConfig {

}
