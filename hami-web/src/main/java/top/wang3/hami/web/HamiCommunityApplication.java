package top.wang3.hami.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import top.wang3.hami.common.dto.Captcha;
import top.wang3.hami.core.HamiCoreConfig;
import top.wang3.hami.core.service.captcha.CaptchaService;
import top.wang3.hami.security.EnableSecurity;

@SpringBootApplication
@Import(value = {HamiCoreConfig.class})
@EnableSecurity
@Slf4j
public class HamiCommunityApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(HamiCommunityApplication.class, args);
        CaptchaService captchaService = context.getBean(CaptchaService.class);
        captchaService.sendCaptcha(new Captcha("1", "2", "3", System.currentTimeMillis()));
        captchaService.sendCaptcha(new Captcha("1", "2", "3", System.currentTimeMillis()));

    }

}