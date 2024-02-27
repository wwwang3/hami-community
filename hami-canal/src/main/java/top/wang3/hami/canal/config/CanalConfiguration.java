package top.wang3.hami.canal.config;


import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.wang3.hami.canal.CanalEntryHandlerFactory;
import top.wang3.hami.canal.converter.CanalMessageConverter;
import top.wang3.hami.canal.converter.FlatCanalMessageConverter;
import top.wang3.hami.canal.converter.ProtobufCanalMessageConverter;

@Configuration
@EnableConfigurationProperties(CanalProperties.class)
@AutoConfigureAfter(RabbitAutoConfiguration.class)
@RequiredArgsConstructor
public class CanalConfiguration {

    @Bean("CanalMessageConverter")
    @ConditionalOnProperty(prefix = "hami.canal", name = "flat-message", havingValue = "false", matchIfMissing = true)
    public CanalMessageConverter canalMessageConverter(CanalEntryHandlerFactory factory) {
        ProtobufCanalMessageConverter converter = new ProtobufCanalMessageConverter();
        converter.setCanalEntryHandlerFactory(factory);
        return converter;
    }

    @Bean("flatCanalMessageConverter")
    @ConditionalOnProperty(prefix = "hami.canal", name = "flat-message", havingValue = "true")
    public CanalMessageConverter flatCanalMessageConverter(CanalEntryHandlerFactory factory) {
        FlatCanalMessageConverter flatCanalMessageConverter = new FlatCanalMessageConverter();
        flatCanalMessageConverter.setCanalEntryHandlerFactory(factory);
        return flatCanalMessageConverter;
    }

}
