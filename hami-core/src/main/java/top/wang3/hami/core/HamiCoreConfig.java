package top.wang3.hami.core;

import cn.xuyanwu.spring.file.storage.spring.EnableFileStorage;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import top.wang3.hami.canal.annotation.EnableCanal;
import top.wang3.hami.common.HamiCommonConfig;
import top.wang3.hami.mail.EnableMail;
import top.wang3.hami.security.EnableSecurity;


@MapperScan(basePackages = "top.wang3.hami.core.mapper")
@Configuration
@EnableAspectJAutoProxy
@EnableTransactionManagement
@EnableScheduling
@EnableMail
@EnableCanal
@EnableSecurity
@EnableFileStorage
@EnableConfigurationProperties(HamiProperties.class)
@Import(value = {HamiCommonConfig.class, CoreConfigurer.class})
public class HamiCoreConfig {

}
