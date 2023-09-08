package top.wang3.hami.message.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import top.wang3.hami.common.constant.Constants;

@Configuration
@ComponentScan(basePackages = {
        "top.wang3.hami.message.listener",
        "top.wang3.hami.message.canal",
})
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
                .durable(Constants.NOTIFY_QUEUE)
                .build();
    }

    @Bean("notify-bind")
    public Binding notifyBind(@Qualifier(Constants.NOTIFY_EXCHANGE) DirectExchange exchange,
                              @Qualifier(Constants.NOTIFY_QUEUE) Queue queue) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(Constants.NOTIFY_ROUTING);
    }


    @Bean(Constants.CANAL_EXCHANGE)
    public DirectExchange canalExchange() {
        return ExchangeBuilder
                .directExchange(Constants.CANAL_EXCHANGE)
                .build();
    }

    @Bean(Constants.CANAL_QUEUE)
    public Queue canalQueue() {
        return QueueBuilder
                .durable(Constants.CANAL_QUEUE)
                .build();
    }

    @Bean
    public Binding canalBinding(@Qualifier(Constants.CANAL_EXCHANGE) DirectExchange exchange,
                                @Qualifier(Constants.CANAL_QUEUE) Queue queue) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(Constants.CANAL_ROUTING);
    }

    /**
     * RabbitMQ消息转化器
     * @return Jackson2JsonMessageConverter
     */
    @Bean("rabbitMQJacksonConverter")
    @Primary
    public Jackson2JsonMessageConverter rabbitMQMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean("simpleMessageConverter")
    public SimpleMessageConverter simpleMessageConverter() {
        return new SimpleMessageConverter();
    }

}
