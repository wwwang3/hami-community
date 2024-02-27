package top.wang3.hami.canal.converter;

import com.alibaba.otter.canal.client.CanalMessageDeserializer;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import top.wang3.hami.canal.CanalEntryHandlerFactory;
import top.wang3.hami.canal.CanalEntryMapper;
import top.wang3.hami.canal.annotation.CanalEntity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Setter
public class ProtobufCanalMessageConverter implements CanalMessageConverter {

    private CanalEntryHandlerFactory canalEntryHandlerFactory;

    public final Map<Class<?>, Map<String, Field>> ENTRY_CACHE = new ConcurrentHashMap<>(32);


    @Override
    public <T> Map<String, List<CanalEntity<T>>> convertToEntity(byte[] bytes) {
        Message message = CanalMessageDeserializer.deserializer(bytes);
        List<CanalEntry.Entry> entries = message.getEntries();
        // 根据表名路由, 其实应该不会出现entries中表名不一样的情况
        HashMap<String, List<CanalEntity<T>>> map = new HashMap<>();
        try {
            for (CanalEntry.Entry entry : entries) {
                if (!CanalEntry.EntryType.ROWDATA.equals(entry.getEntryType())) {
                    continue;
                }
                String tableName = entry.getHeader().getTableName();
                CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
                List<CanalEntry.RowData> rowDatasList = rowChange.getRowDatasList();
                if (rowDatasList.isEmpty()) {
                    // empty
                    continue;
                }
                CanalEntry.EventType eventType = rowChange.getEventType();
                if (!isSupportedType(eventType)) {
                    continue;
                }
                List<CanalEntity<T>> entities = map.computeIfAbsent(tableName, k -> new ArrayList<>());
                processRowDataList(tableName, rowDatasList, entities, eventType);
            }
        } catch (Exception e) {
            log.error("deserialize message to entity failed, error_class: {}, error_message: {}",
                    e.getClass(),
                    e.getMessage());
        }
        return map;
    }

    private <T> void processRowDataList(String tableName,
                                        List<CanalEntry.RowData> rowDatasList,
                                        List<CanalEntity<T>> entities,
                                        CanalEntry.EventType type) throws JsonProcessingException,
            InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        for (CanalEntry.RowData rowData : rowDatasList) {
            CanalEntity<T> entity = rowDataToEntity(tableName, rowData, type);
            if (entity.getBefore() != null || entity.getAfter() != null) {
                // ignore before and after is null
                entities.add(entity);
            }
        }
    }

    private <T> CanalEntity<T> rowDataToEntity(String tableName, CanalEntry.RowData rowData, CanalEntry.EventType eventType)
            throws JsonProcessingException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        CanalEntity<T> entity = new CanalEntity<>();
        Class<T> tableClass = canalEntryHandlerFactory.getTableClass(tableName);
        Map<String, Field> tableField = canalEntryHandlerFactory.getTableField(tableName);
        entity.setTableName(tableName);
        entity.setTableClass(tableClass);
        entity.setType(eventType);
        switch (eventType) {
            case INSERT -> {
                T after = getEntity(rowData.getAfterColumnsList(), tableClass, tableField);
                entity.setAfter(after);
            }
            case UPDATE -> {
                T before = getEntity(rowData.getBeforeColumnsList(), tableClass, tableField);
                T after = getEntity(rowData.getAfterColumnsList(), tableClass, tableField);
                entity.setBefore(before);
                entity.setAfter(after);
            }
            case DELETE -> {
                T before = getEntity(rowData.getBeforeColumnsList(), tableClass, tableField);
                entity.setBefore(before);
            }
        }
        return entity;
    }

    private <T> T getEntity(List<CanalEntry.Column> columns, Class<T> tableClass, Map<String, Field> tableField)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        T instance = tableClass.getDeclaredConstructor().newInstance();
        for (CanalEntry.Column column : columns) {
            String name = column.getName();
            Field field = tableField.get(name);
            if (field != null) {
                field.setAccessible(true);
                field.set(instance, CanalEntryMapper.convertType(field.getType(), column.getValue()));
            }
        }
        return instance;
    }

    private boolean isSupportedType(CanalEntry.EventType type) {
        // only insert update delete supported
        return type != null && type.ordinal() <= 2;
    }

}
