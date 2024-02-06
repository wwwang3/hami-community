package top.wang3.hami.security.config;


import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import top.wang3.hami.security.listener.AuthenticationEventListener;

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(RabbitAutoConfiguration.class)
public class AuthenticationEventConfig {

    @Bean(initMethod = "init")
    @ConditionalOnBean(value = {AuthenticationEventPublisher.class, RabbitTemplate.class})
    public AuthenticationEventListener authenticationEventListener(RabbitTemplate rabbitTemplate) {
        AuthenticationEventListener listener = new AuthenticationEventListener();
        listener.setRabbitTemplate(rabbitTemplate);
        return listener;
    }

}
