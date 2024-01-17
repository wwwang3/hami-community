package top.wang3.hami.web;

import cn.xuyanwu.spring.file.storage.spring.EnableFileStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import top.wang3.hami.canal.annotation.EnableCanal;
import top.wang3.hami.core.HamiCoreConfig;
import top.wang3.hami.mail.EnableMail;
import top.wang3.hami.security.EnableSecurity;

@SpringBootApplication
@Import(value = {HamiCoreConfig.class})
@EnableSecurity
@EnableTransactionManagement
@EnableFileStorage
@EnableMail
@EnableCanal
@EnableAspectJAutoProxy
@Slf4j
public class HamiCommunityApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(HamiCommunityApplication.class, args);
    }

}