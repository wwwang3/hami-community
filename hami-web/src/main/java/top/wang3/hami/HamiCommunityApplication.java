package top.wang3.hami;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import top.wang3.hami.security.EnableSecurity;

@SpringBootApplication
@EnableSecurity
public class HamiCommunityApplication {

    public static void main(String[] args) {
        SpringApplication.run(HamiCommunityApplication.class, args);
    }

}
