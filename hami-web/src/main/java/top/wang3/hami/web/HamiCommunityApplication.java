package top.wang3.hami.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import top.wang3.hami.core.HamiCoreConfig;
import top.wang3.hami.security.EnableSecurity;

@SpringBootApplication
@Import(value = {HamiCoreConfig.class})
@EnableSecurity
@Slf4j
public class HamiCommunityApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(HamiCommunityApplication.class, args);
    }

}