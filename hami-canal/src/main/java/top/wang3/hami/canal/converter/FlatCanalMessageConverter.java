package top.wang3.hami.canal.converter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.otter.canal.protocol.FlatMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import top.wang3.hami.canal.CanalEntryHandlerFactory;
import top.wang3.hami.canal.CanalEntryMapper;
import top.wang3.hami.canal.annotation.CanalEntity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Slf4j
@Setter
public class FlatCanalMessageConverter implements CanalMessageConverter {


    private CanalEntryHandlerFactory canalEntryHandlerFactory;

    @Override
    public <T> Map<String, List<CanalEntity<T>>> convertToEntity(byte[] bytes) {
        HashMap<String, List<CanalEntity<T>>> map = new HashMap<>();
        try {
            FlatMessage flatMessage = JSON.parseObject(bytes, FlatMessage.class);
            List<Map<String, String>> data = flatMessage.getData();
            if (!data.isEmpty()) {
                ArrayList<CanalEntity<T>> entities = new ArrayList<>(data.size());
                processData(flatMessage, entities);
                map.put(flatMessage.getTable(), entities);
            }
        } catch (Exception e) {
            log.error("deserialize message to entity failed, error_class: {}, error_message: {}",
                    e.getClass(),
                    e.getMessage());
        }
        return map;
    }

    private <T> void processData(FlatMessage flatMessage, ArrayList<CanalEntity<T>> entities) throws JsonProcessingException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        List<Map<String, String>> data = flatMessage.getData();
        List<Map<String, String>> old = flatMessage.getOld();
        String type = flatMessage.getType();
        String table = flatMessage.getTable();
        Class<T> tableClass = canalEntryHandlerFactory.getTableClass(table);
        int length = data.size();
        for (int i = 0; i < length; i++) {
            CanalEntity<T> canalEntity = new CanalEntity<>();
            canalEntity.setTableClass(tableClass);
            canalEntity.setTableName(table);
            switch (type) {
                case "INSERT" -> {
                    canalEntity.setBefore(mapToEntity(table, tableClass, data.get(i)));
                    entities.add(canalEntity);
                }
                case "UPDATE" -> {
                    canalEntity.setBefore(mapToEntity(table, tableClass, old.get(i)));
                    canalEntity.setAfter(mapToEntity(table, tableClass, data.get(i)));
                    entities.add(canalEntity);
                }
                case "DELETE" -> {
                    canalEntity.setAfter(mapToEntity(table, tableClass, data.get(i)));
                    entities.add(canalEntity);
                }
            }
        }
    }

    private <T> T mapToEntity(String tableName, Class<T> tableClass, Map<String, String> map) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, JsonProcessingException {
        final Map<String, Field> tableField = canalEntryHandlerFactory.getTableField(tableName);
        T instance = tableClass.getDeclaredConstructor()
                .newInstance();
        Set<String> keySet = map.keySet();
        for (String key : keySet) {
            Field field = tableField.get(key);
            if (field != null) {
                field.setAccessible(true);
                field.set(instance, CanalEntryMapper.convertType(field.getType(), map.get(key)));
            }
        }
        return instance;
    }
}
