package top.wang3.hami.canal.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "hami.canal")
public class HamiCanalProperties {

    private boolean flatMessage = false;

    private String exchange;

    private String exchangeType;

    @NestedConfigurationProperty
    private Map<String, CanalMessageContainer> containers;


    /**
     * RabbitMQ MessageListenerContainer容器配置,
     * 一个CanalContainer对应一个 MessageListenerContainer
     * 建议需要顺序消费的表定义单独的容器, 若要在多个容器监听同一张表, 可以在@CanalRabbitHandler指定容器ID
     * @see top.wang3.hami.canal.annotation.CanalRabbitHandler
     */
    @Data
    public static class CanalMessageContainer {

        /**
         * 该容器要绑定到交换机的队列
         * 若多个容器绑定了相同routingKey的队列, 会造成消息重复消费
         */
        @NestedConfigurationProperty
        private List<Queue> queues;

        /**
         * 同RabbitListener
         */
        private String concurrency;

    }

    @Data
    public static class Queue {

        /**
         * 该队列名称
         */
        String name;

        /**
         * 队列绑定到交换机的路由
         */
        String routingKey;


    }


}
