package top.wang3.hami.canal.config;


import lombok.RequiredArgsConstructor;
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
import top.wang3.hami.canal.CanalEntryHandlerFactory;
import top.wang3.hami.canal.converter.CanalMessageConverter;
import top.wang3.hami.canal.listener.CanalListenerEndpoint;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class CanalListenerConfigurer implements RabbitListenerConfigurer, BeanFactoryAware, SmartInitializingSingleton {

    private CanalEntryHandlerFactory canalEntryHandlerFactory;

    private CanalMessageConverter messageConverter;

    private HamiCanalProperties hamiCanalProperties;

    private BeanFactory beanFactory;
    private int increment = 0;

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        // todo 注册CanalListenerContainer
        Map<String, HamiCanalProperties.CanalMessageContainer> containers =
                hamiCanalProperties.getContainers();
        Set<String> keySet = containers.keySet();
        Exchange exchange = registerExchange(hamiCanalProperties.getExchange(), hamiCanalProperties.getExchangeType());
        for (String id : keySet) {
            CanalListenerEndpoint endpoint = new CanalListenerEndpoint();
            endpoint.setCanalEntryHandlerFactory(canalEntryHandlerFactory);
            endpoint.setCanalMessageConverter(messageConverter);
            endpoint.setId(id);
            HamiCanalProperties.CanalMessageContainer container = containers.get(id);
            registerQueue(container.getQueues());
            registerBinding(exchange, container.getQueues());
            processEndpoint(endpoint, container);
            registrar.registerEndpoint(endpoint);
        }
    }

    private void registerBinding(Exchange exchange, List<HamiCanalProperties.Queue> queues) {
        for (HamiCanalProperties.Queue queue : queues) {
            Binding binding = new Binding(
                    queue.getName(),
                    Binding.DestinationType.QUEUE,
                    exchange.getName(),
                    queue.getRoutingKey(),
                    null
            );
            String beanName = exchange.getName() + "." + queue.getName() + ++increment;
            ((ConfigurableBeanFactory) beanFactory).registerSingleton(beanName, binding);
        }
    }

    private void registerQueue(List<HamiCanalProperties.Queue> queues) {
        for (HamiCanalProperties.Queue queue : queues) {
            Queue amqpQueue = new Queue(queue.getName(), true);
            ((ConfigurableBeanFactory) beanFactory).registerSingleton(queue.getName() + (++increment), amqpQueue);
        }
    }

    private Exchange registerExchange(String exchange, String exchangeType) {
        ExchangeBuilder exchangeBuilder = new ExchangeBuilder(exchange, exchangeType);
        Exchange ex = exchangeBuilder.durable(true)
                .build();
        ((ConfigurableBeanFactory) this.beanFactory).registerSingleton(exchange, ex);
        return ex;
    }

    private void processEndpoint(CanalListenerEndpoint endpoint,
                                 HamiCanalProperties.CanalMessageContainer canalMessageContainer) {
        endpoint.setConcurrency(canalMessageContainer.getConcurrency());
        List<HamiCanalProperties.Queue> queues = canalMessageContainer.getQueues();
        Queue[] queueNames = queues.stream().map(q -> new Queue(q.getName(), true)).toArray(Queue[]::new);
        endpoint.setQueues(queueNames);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterSingletonsInstantiated() {
        this.canalEntryHandlerFactory = beanFactory.getBean(
                HamiCanalRegistrar.CANAL_ENTRY_HANDLER_FACTORY, CanalEntryHandlerFactory.class);
        this.hamiCanalProperties = beanFactory.getBean(HamiCanalProperties.class);
        this.messageConverter = beanFactory.getBean(CanalMessageConverter.class);
    }
}
