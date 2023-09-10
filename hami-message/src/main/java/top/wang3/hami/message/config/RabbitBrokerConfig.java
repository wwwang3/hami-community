package top.wang3.hami.message.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import top.wang3.hami.common.constant.Constants;

@Configuration
@ComponentScan(basePackages = {
        "top.wang3.hami.message.listener",
})
public class RabbitBrokerConfig {

    @Bean(Constants.HAMI_DIRECT_EXCHANGE1)
    public DirectExchange directExchange() {
        return ExchangeBuilder
                .directExchange(Constants.HAMI_DIRECT_EXCHANGE1)
                .build();
    }

    @Bean(Constants.HAMI_DIRECT_EXCHANGE2)
    public DirectExchange notifyDirectExchange() {
        return ExchangeBuilder
                .directExchange(Constants.HAMI_DIRECT_EXCHANGE2)
                .build();
    }

    @Bean(Constants.EMAIL_QUEUE)
    public Queue emailQueue() {
        return QueueBuilder
                .durable(Constants.EMAIL_QUEUE)
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


    @Bean(Constants.NOTIFY_QUEUE)
    public Queue notifyQueue() {
        return QueueBuilder
                .durable(Constants.NOTIFY_QUEUE)
                .build();
    }

    @Bean("notify-bind")
    public Binding notifyBind(@Qualifier(Constants.HAMI_DIRECT_EXCHANGE1) DirectExchange exchange,
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

    @Bean(Constants.READING_RECORD_QUEUE)
    public Queue readingRecordQueue() {
        return QueueBuilder
                .durable(Constants.READING_RECORD_QUEUE)
                .build();
    }

    @Bean
    public Binding readingRecordBinding(@Qualifier(Constants.HAMI_DIRECT_EXCHANGE2) DirectExchange exchange,
                                       @Qualifier(Constants.READING_RECORD_QUEUE) Queue queue) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(Constants.READING_RECORD_ROUTING);
    }

    @Bean(Constants.ADD_VIEWS_QUEUE)
    public Queue addViewsQueue() {
        return QueueBuilder
                .durable(Constants.ADD_VIEWS_QUEUE)
                .build();
    }

    @Bean
    public Binding viewsBinding(@Qualifier(Constants.HAMI_DIRECT_EXCHANGE2) DirectExchange exchange,
                              @Qualifier(Constants.ADD_VIEWS_QUEUE) Queue queue) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(Constants.ADD_VIEWS_ROUTING);
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
