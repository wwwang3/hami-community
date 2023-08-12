package top.wang3.hami.mail;


import org.springframework.context.annotation.Import;
import top.wang3.hami.mail.config.CustomMailAutoConfiguration;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import(value = {CustomMailAutoConfiguration.class})
public @interface EnableMail {
}
