package top.wang3.hami.canal.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "hami.canal")
public class CanalProperties {

    /**
     * 数据库名
     */
    private String schema;

    /**
     * 交换机名称
     */
    private String exchange;

    /**
     * 交换机类型
     */
    private String exchangeType;

    /**
     * 发送的是否是FlatMessage格式的数据
     */
    private boolean flatMessage = false;

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
         * 该容器要监听的表, 一张表一个队列
         * 若多个容器监听了相同的表, 且@CanalRabbitHandler注解上没有提供containerId, 会造成消息的重复消费
         */
        @NestedConfigurationProperty
        private List<Table> tables;

        /**
         * 同RabbitListener
         */
        private String concurrency = "1";

    }

    @Data
    public static class Table {

        /**
         * 表名
         */
        private String name;

        /**
         * 该表生成的队列绑定到交换机的路由, 可以为空或null
         * 默认为schema + '_' + name 数据库名_表名
         */
        private String routingKey;

    }


}
