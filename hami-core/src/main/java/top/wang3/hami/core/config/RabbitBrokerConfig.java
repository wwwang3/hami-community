package top.wang3.hami.core.config;


import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.NonNull;

@Configuration
public class RabbitBrokerConfig {

    @Bean("batchRabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory batchRabbitListenerContainerFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer,
                                                                            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory containerFactory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(containerFactory, connectionFactory);
        containerFactory.setBatchSize(50);
        containerFactory.setBatchListener(true);
        containerFactory.setConsumerBatchEnabled(true);
        containerFactory.setConnectionFactory(connectionFactory);
        containerFactory.setReceiveTimeout(200L); // 每次收到消息前最多阻塞等待的时长 200 * 50 = 10s 最多10s才写入
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
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }

    @Bean("canalMessageConverter")
    public MessageConverter canalMessageConverter() {
        //fix: 兼容Canal1.1.7改变, content-type设置为text/plain会被转化为String类型
        return new SimpleMessageConverter() {

            @Override
            @NonNull
            public Object fromMessage(@NonNull Message message) throws MessageConversionException {
                return message.getBody();
            }

        };
    }

}
