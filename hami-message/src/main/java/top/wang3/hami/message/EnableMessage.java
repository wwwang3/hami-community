package top.wang3.hami.message;


import org.springframework.context.annotation.Import;
import top.wang3.hami.mail.EnableMail;
import top.wang3.hami.message.config.RabbitBrokerConfig;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import(RabbitBrokerConfig.class)
@EnableMail
public @interface EnableMessage {
}
