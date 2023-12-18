package top.wang3.hami.canal.config;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import top.wang3.hami.canal.CanalEntryHandlerFactory;
import top.wang3.hami.canal.converter.CanalMessageConverter;
import top.wang3.hami.canal.listener.CanalListenerEndpoint;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class CanalListenerConfigurer implements RabbitListenerConfigurer,
        BeanFactoryAware, SmartInitializingSingleton {

    private CanalEntryHandlerFactory canalEntryHandlerFactory;

    private CanalMessageConverter messageConverter;

    private CanalProperties canalProperties;

    private BeanFactory beanFactory;
    private int queueSize = 0;
    private int bindingSize = 0;

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        var containers = canalProperties.getContainers();
        log.info("start to register CanalMessageListenerContainer, found {} container-config", containers.size());
        // 注册交换机
        registerExchange(canalProperties.getExchange(), canalProperties.getExchangeType());
        var entries = containers.entrySet();
        for (var entry : entries) {
            String id = entry.getKey();
            var container = entry.getValue();
            CanalListenerEndpoint endpoint = new CanalListenerEndpoint();
            // 容器ID
            endpoint.setId(id);
            // 注册endpoint
            processEndpoint(endpoint, container);
            registrar.registerEndpoint(endpoint);
            log.info("process endpoint success, container-id: {}", endpoint.getId());
        }
    }

    private void processEndpoint(CanalListenerEndpoint endpoint,
                                 CanalProperties.CanalMessageContainer container) {
        endpoint.setConcurrency(container.getConcurrency());
        endpoint.setCanalEntryHandlerFactory(canalEntryHandlerFactory);
        endpoint.setCanalMessageConverter(messageConverter);
        // 注册队列
        List<Queue> queues = registerQueue(container.getTables());
        endpoint.setQueues(queues.toArray(Queue[]::new));
    }

    private List<Queue> registerQueue(List<CanalProperties.Table> tables) {
        ArrayList<Queue> queues = new ArrayList<>(tables.size());
        for (CanalProperties.Table table : tables) {
            String queueOrBeanName = "canal-" + table.getName() + "-" + ++queueSize;
            Queue amqpQueue = new Queue(queueOrBeanName, true);
            // 向容器注册bean, Spring会帮我们声明队列
            ((ConfigurableBeanFactory) beanFactory).registerSingleton(queueOrBeanName, amqpQueue);
            registerBinding(table.getName(), queueOrBeanName, table.getRoutingKey());
            queues.add(amqpQueue);
        }
        return queues;
    }

    private void registerBinding(String tableName, String queueName, String routingKey) {
        final String schema = canalProperties.getSchema();
        final String exchange = canalProperties.getExchange();
        String route = buildRoutingKey(schema, tableName, routingKey);
        Binding binding = new Binding(
                queueName,
                Binding.DestinationType.QUEUE,
                exchange,
                route,
                null
        );
        String beanName = exchange + "." + queueName + ++bindingSize;
        ((ConfigurableBeanFactory) beanFactory).registerSingleton(beanName, binding);
        log.info("register binding to exchange [{}] success: queue: {}, routing: {}",
                exchange, queueName, route);
    }

    private void registerExchange(String exchange, String exchangeType) {
        ExchangeBuilder exchangeBuilder = new ExchangeBuilder(exchange, exchangeType);
        Exchange ex = exchangeBuilder.durable(true)
                .build();
        ((ConfigurableBeanFactory) this.beanFactory).registerSingleton(exchange, ex);
        log.info("register canal exchange: {} success", ex.getName());
    }

    private String buildRoutingKey(String schema, String tableName, String routingKey) {
        if (StringUtils.hasText(routingKey)) {
            return routingKey;
        }
        return schema + "_" + tableName;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterSingletonsInstantiated() {
        this.canalEntryHandlerFactory = beanFactory.getBean(
                CanalRegistrar.CANAL_ENTRY_HANDLER_FACTORY, CanalEntryHandlerFactory.class);
        this.canalProperties = beanFactory.getBean(CanalProperties.class);
        this.messageConverter = beanFactory.getBean(CanalMessageConverter.class);
    }
}
