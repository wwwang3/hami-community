package top.wang3.hami.message;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import top.wang3.hami.mail.EnableMail;

@SpringBootApplication(exclude = {
        ThymeleafAutoConfiguration.class,
        WebFluxAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        DataSourceAutoConfiguration.class
})
@EnableMail
public class HamiMessageApplication {

    public static void main(String[] args) {
        SpringApplication.run(HamiMessageApplication.class, args);
    }
}
