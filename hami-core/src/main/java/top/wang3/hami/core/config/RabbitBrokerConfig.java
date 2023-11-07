package top.wang3.hami.core.config;


import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RabbitBrokerConfig {

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
