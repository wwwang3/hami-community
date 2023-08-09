package top.wang3.hami.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import top.wang3.hami.core.EnableCoreConfig;
import top.wang3.hami.security.EnableSecurity;

@SpringBootApplication
@EnableSecurity
@EnableCoreConfig
public class HamiCommunityApplication {

    public static void main(String[] args) {
        SpringApplication.run(HamiCommunityApplication.class, args);
    }

}