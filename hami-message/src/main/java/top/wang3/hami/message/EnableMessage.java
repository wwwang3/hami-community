package top.wang3.hami.message;


import org.springframework.context.annotation.ComponentScan;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
//@Import({RabbitBrokerConfig.class})
@ComponentScan(basePackages = {"top.wang3.hami.message"})
public @interface EnableMessage {
}
