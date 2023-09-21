package top.wang3.hami.core.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import top.wang3.hami.common.constant.Constants;

@Configuration
public class RabbitBrokerConfig {

    @Bean(Constants.HAMI_DIRECT_EXCHANGE1)
    public DirectExchange directExchange() {
        return ExchangeBuilder
                .directExchange(Constants.HAMI_DIRECT_EXCHANGE1)
                .build();
    }


    @Bean(Constants.EMAIL_QUEUE)
    public Queue emailQueue() {
        return QueueBuilder
                .nonDurable(Constants.EMAIL_QUEUE)
                .build();
    }

    @Bean
    public Binding emailBind(@Qualifier(Constants.HAMI_DIRECT_EXCHANGE1) DirectExchange exchange,
                             @Qualifier(Constants.EMAIL_QUEUE) Queue queue) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(Constants.EMAIL_ROUTING);
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

    @Bean
    public SimpleRabbitListenerContainerFactory batchRabbitContainerFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer,
                                                                            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory containerFactory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(containerFactory, connectionFactory);
        containerFactory.setBatchSize(20);
        containerFactory.setBatchListener(true);
        containerFactory.setConsumerBatchEnabled(true);
        containerFactory.setConnectionFactory(connectionFactory);
        containerFactory.setReceiveTimeout(400L);
        return containerFactory;
    }

    /**
     * RabbitMQ消息转化器
     *
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
