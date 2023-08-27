package top.wang3.hami.message.config;


import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
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
    public Binding emailBind(@Qualifier(Constants.EMAIL_EXCHANGE) DirectExchange exchange,
                             @Qualifier(Constants.EMAIL_QUEUE) Queue queue) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(Constants.EMAIL_ROUTING);
    }

    @Bean(Constants.NOTIFY_EXCHANGE)
    public DirectExchange notifyDirectExchange() {
        return ExchangeBuilder
                .directExchange(Constants.NOTIFY_EXCHANGE)
                .build();
    }

    @Bean(Constants.NOTIFY_QUEUE)
    public Queue notifyQueue() {
        return QueueBuilder
                .durable(Constants.EMAIL_QUEUE)
                .build();
    }

    @Bean("notify-bind")
    public Binding notifyBind(@Qualifier(Constants.NOTIFY_EXCHANGE) DirectExchange exchange,
                              @Qualifier(Constants.NOTIFY_QUEUE) Queue queue) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(Constants.EMAIL_ROUTING);
    }


}
