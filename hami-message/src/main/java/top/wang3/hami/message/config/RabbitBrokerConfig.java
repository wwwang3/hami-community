package top.wang3.hami.message.config;


import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.wang3.hami.common.constant.Constants;

@Configuration
public class RabbitBrokerConfig {

    @Bean(Constants.EMAIL_EXCHANGE)
    public DirectExchange directExchange() {
        return ExchangeBuilder
                .directExchange(Constants.EMAIL_EXCHANGE)
                .build();
    }

    @Bean(Constants.EMAIL_QUEUE)
    public Queue emailQueue() {
        return QueueBuilder
                .durable(Constants.EMAIL_QUEUE)
                .build();
    }

    @Bean
    public Binding emailBind(DirectExchange exchange, Queue queue) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(Constants.EMAIL_ROUTING);
    }


}
