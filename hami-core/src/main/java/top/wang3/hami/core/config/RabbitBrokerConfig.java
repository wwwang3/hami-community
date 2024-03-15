package top.wang3.hami.core.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import top.wang3.hami.common.constant.RabbitConstants;

import static top.wang3.hami.common.constant.RabbitConstants.*;

@Configuration
public class RabbitBrokerConfig {

    @Bean(RabbitConstants.BATCH_LISTENER_FACTORY)
    public SimpleRabbitListenerContainerFactory batchRabbitListenerContainerFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer,
                                                                            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory containerFactory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(containerFactory, connectionFactory);
        containerFactory.setBatchSize(20);
        containerFactory.setBatchListener(true);
        containerFactory.setConsumerBatchEnabled(true);
        containerFactory.setConnectionFactory(connectionFactory);
        containerFactory.setReceiveTimeout(500L); // 每次收到消息前最多阻塞等待的时长 20 * 500 = 10s 最多10s才写入
        return containerFactory;
    }

    @Bean(HAMI_DL_QUEUE)
    public Queue deadLetterQueue() {
        return QueueBuilder
            .durable(HAMI_DL_QUEUE)
            .build();
    }

    @Bean(HAMI_DL_EXCHANGE)
    public Exchange deadLetterExchange() {
        return ExchangeBuilder
            .directExchange(HAMI_DL_EXCHANGE)
            .build();
    }

    @Bean
    public Binding deadLetterBinding(@Qualifier(HAMI_DL_QUEUE) Queue queue, @Qualifier(HAMI_DL_EXCHANGE) Exchange exchange) {
        return BindingBuilder.bind(queue)
            .to(exchange)
            .with(HAMI_DL_ROUTING)
            .noargs();
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

}
