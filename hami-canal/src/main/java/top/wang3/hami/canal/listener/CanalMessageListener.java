package top.wang3.hami.canal.listener;

import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.canal.CanalEntryHandler;
import top.wang3.hami.canal.CanalEntryHandlerFactory;
import top.wang3.hami.canal.annotation.CanalEntity;
import top.wang3.hami.canal.converter.CanalMessageConverter;

import java.util.List;
import java.util.Map;

@Slf4j
public class CanalMessageListener implements ChannelAwareMessageListener {

    private final String containerId;
    private final CanalEntryHandlerFactory factory;
    private final CanalMessageConverter canalMessageConverter;

    private boolean isManual;

    public CanalMessageListener(String containerId, CanalEntryHandlerFactory factory,
                                CanalMessageConverter canalMessageConverter) {
        this.containerId = containerId;
        this.factory = factory;
        this.canalMessageConverter = canalMessageConverter;
    }

    @Override
    public void containerAckMode(AcknowledgeMode mode) {
        isManual = mode.isManual();
    }

    public void onMessage(Message message, Channel channel) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("canal-container: {}, received a message", containerId);
        }
        long start = System.currentTimeMillis();
        Map<String, List<CanalEntity<Object>>> entitiesMap  = deserializeMessage(message);
        try {
            int size = 0;
            for (Map.Entry<String, List<CanalEntity<Object>>> entry : entitiesMap.entrySet()) {
                String key = entry.getKey();
                List<CanalEntity<Object>> entities = entry.getValue();
                if (entities.isEmpty()) {
                    continue;
                }
                List<CanalEntryHandler<?>> handlers = findHandler(containerId, key);
                if (CollectionUtils.isEmpty(handlers)) {
                    log.warn("no handler found for table: {}", key);
                    continue;
                }
                size += entities.size();
                processEntity(entities, handlers);
            }
            long end = System.currentTimeMillis();
            if (log.isDebugEnabled()) {
                log.debug("container: [{}] handle {} message, cost: {}ms", containerId, size, end - start);
            }
            if (isManual) {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        } catch (Exception e) {
            log.error(
                    "handle message failed, container-id: {}, error_class: {}, error_msg: {}, entitiesMap: {}",
                    containerId, e.getClass(),
                    e.getMessage(), entitiesMap
            );
            if (isManual) {
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            } else {
                throw e;
            }
        }
    }

    @SneakyThrows
    @Override
    public void onMessageBatch(List<Message> messages, Channel channel) {
        for (Message message : messages) {
            onMessage(message, channel);
        }
    }

    private Map<String, List<CanalEntity<Object>>> deserializeMessage(Message message) {
        try {
            return canalMessageConverter.convertToEntity(message.getBody());
        } catch (Exception e) {
            log.error("CanalMessageConverter serialize message failed. container-id: {}, error_class: {}, error_msg: {}",
                    containerId, e.getClass().getName(), e.getMessage());
            throw new MessageConversionException("CanalMessageConverter serialize message failed.", e);
        }
    }

    private void processEntity(List<CanalEntity<Object>> entities, List<CanalEntryHandler<?>> handlers) {
        for (CanalEntity<?> entity : entities) {
            // 顺序消费
            for (CanalEntryHandler<?> handler : handlers) {
                handle(handler, entity);
                if (log.isDebugEnabled()) {
                    log.debug("canal-handler: {} handle message success.", handler.getClass().getSimpleName());
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void handle(CanalEntryHandler<T> handler, CanalEntity<?> entity) {
        switch (entity.getType()) {
            case INSERT -> handler.processInsert((T) entity.getAfter());
            case UPDATE -> handler.processUpdate((T) entity.getBefore(), (T) entity.getAfter());
            case DELETE -> handler.processDelete((T) entity.getBefore());
        }
    }

    private List<CanalEntryHandler<?>> findHandler(String containerId, String tableName) {
        List<CanalEntryHandler<?>> containerHandler = factory.getContainerHandlers(containerId, tableName);
        if (containerHandler == null || containerHandler.isEmpty()) {
            return factory.getHandler(tableName);
        }
        return containerHandler;
    }
}
