package top.wang3.hami.canal.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Canal RabbitMQ消费者
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CanalRabbitHandler {

    /**
     * 处理的table名称
     * rabbitmq不支持消息分区, 对同一张表来说, 路由键应该是固定的
     * @return table表名称
     */
    String value() default "";


    /**
     * CanalMessageContainer的ID, 也就是配置时的mapKey
     * 处理消息时, 若配置了container, 则只会处理对应的container监听到的消息
     * 不指定container则执行对应表所有的CanalEntryHandler
     * @return containerID
     */
    String container() default "";

}
