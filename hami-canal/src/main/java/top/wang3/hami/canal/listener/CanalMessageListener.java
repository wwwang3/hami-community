package top.wang3.hami.canal.listener;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.canal.CanalEntryHandler;
import top.wang3.hami.canal.CanalEntryHandlerFactory;
import top.wang3.hami.canal.annotation.CanalEntity;
import top.wang3.hami.canal.converter.CanalMessageConverter;
import top.wang3.hami.canal.converter.FlatCanalMessageConverter;

import java.util.List;
import java.util.Map;

@Slf4j
public class CanalMessageListener implements ChannelAwareMessageListener {

    private final String containerId;
    private final CanalEntryHandlerFactory factory;
    private final CanalMessageConverter canalMessageConverter;

    public CanalMessageListener(String containerId, CanalEntryHandlerFactory factory,
                                CanalMessageConverter canalMessageConverter) {
        this.containerId = containerId;
        this.factory = factory;
        this.canalMessageConverter = canalMessageConverter;
    }

    @SuppressWarnings("all")
    public void onMessage(Message message, Channel channel) {
        if (log.isDebugEnabled()) {
            log.debug("canal-container: {}, received a message", containerId);
        }
        long start = System.currentTimeMillis();
        if (isEmptyMessage(message)) {
            return;
        }
        Map<String, List<CanalEntity<Object>>> entitiesMap  = deserializeMessage(message);
        if (entitiesMap.isEmpty()) return;
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
        } catch (Exception e) {
            e.printStackTrace();
            log.error(
                    "handle message failed, container-id: {}, error_class: {}, error_msg: {}, entitiesMap: {}",
                    containerId, e.getClass(),
                    e.getMessage(), entitiesMap
            );
        }
    }

    private Map<String, List<CanalEntity<Object>>> deserializeMessage(Message message) {
        if (message.getMessageProperties()
                .getContentType()
                .equalsIgnoreCase("application/json") && !(canalMessageConverter instanceof FlatCanalMessageConverter)) {
            throw new IllegalStateException("canal send a flat-message, " +
                                            "but converter is not FlatMessageConverter, " +
                                            "Please restart app and set property: hami.canal.flat-message=true");
        }
        return canalMessageConverter.convertToEntity(message.getBody());
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

    private boolean isEmptyMessage(Message message) {
        return message == null || message.getBody() == null || message.getBody().length == 0;
    }

    private List<CanalEntryHandler<?>> findHandler(String containerId, String tableName) {
        List<CanalEntryHandler<?>> containerHandler = factory.getContainerHandlers(containerId, tableName);
        if (containerHandler == null || containerHandler.isEmpty()) {
            return factory.getHandler(tableName);
        }
        return containerHandler;
    }
}
