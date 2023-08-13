package top.wang3.hami.core.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.wang3.hami.common.constant.Constants;

@Configuration
public class RabbitMQConfig {

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


    /**
     * RabbitMQ消息转化器
     * @return Jackson2JsonMessageConverter
     */
    @Bean("rabbitMQJacksonConverter")
    @ConditionalOnMissingBean
    public Jackson2JsonMessageConverter rabbitMQMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }


}
