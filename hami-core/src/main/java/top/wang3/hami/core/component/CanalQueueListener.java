package top.wang3.hami.core.component;


import com.alibaba.otter.canal.client.CanalMessageDeserializer;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.canal.CanalEntryHandler;
import top.wang3.hami.common.canal.CanalEntryHandlerFactory;
import top.wang3.hami.common.canal.CanalEntryMapper;
import top.wang3.hami.common.constant.Constants;

import java.util.List;

/**
 * todo FlatMessage支持
 */
@Component
@RabbitListener(queues = {Constants.CANAL_QUEUE}, messageConverter = "simpleMessageConverter",
        concurrency = "16")
@Slf4j
public class CanalQueueListener {

    @PostConstruct
    public void init() {
        log.debug("rabbit listener CanalQueueListener register for use");
    }

    @Resource
    CanalEntryHandlerFactory factory;

    @RabbitHandler
    public void handleCanalMessage(byte[] content) {
        long start = System.currentTimeMillis();
        Message message = CanalMessageDeserializer.deserializer(content);
        if (message == null) return;
        List<CanalEntry.Entry> entries = message.getEntries();
        for (CanalEntry.Entry entry : entries) {
            //row-data
            if (CanalEntry.EntryType.ROWDATA.equals(entry.getEntryType())) {
                try {
                    String tableName = entry.getHeader().getTableName();
                    List<CanalEntryHandler<?>> handlers = factory.getHandler(tableName);
                    if (handlers != null && !handlers.isEmpty()) {
                        CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
                        log.debug("event type: {}", rowChange.getEventType());
                        List<CanalEntry.RowData> rowDataList = rowChange.getRowDatasList();
                        for (CanalEntry.RowData rowData : rowDataList) {
                            for (CanalEntryHandler<?> handler : handlers) {
                                processRowData(rowData, handler, rowChange.getEventType());
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //todo 处理失败重试
                    log.debug("process CanalEntry failed: error_class: {} error_msg: {}",
                            e.getClass().getSimpleName(), e.getMessage());
                }
            }
        }
        long end = System.currentTimeMillis();
        log.debug("## process mysql data change cost: {}", end - start);
    }

    private <T> void processRowData(CanalEntry.RowData rowData, CanalEntryHandler<T> handler, CanalEntry.EventType type) throws Exception {
        switch (type) {
            case INSERT: {
                T t = CanalEntryMapper.mapToEntity(rowData.getAfterColumnsList(), handler);
                handler.processInsert(t);
                break;
            }
            case DELETE: {
                T t = CanalEntryMapper.mapToEntity(rowData.getBeforeColumnsList(), handler);
                handler.processDelete(t);
                break;
            }
            case UPDATE: {
                T before = CanalEntryMapper.mapToEntity(rowData.getBeforeColumnsList(), handler);
                T after = CanalEntryMapper.mapToEntity(rowData.getAfterColumnsList(), handler);
                handler.processUpdate(before, after);
            }
            default:
                break;
        }
    }


}
