package top.wang3.hami.canal.converter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.FlatMessage;
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
    public <T> Map<String, List<CanalEntity<T>>> convertToEntity(byte[] bytes) throws Exception {
        HashMap<String, List<CanalEntity<T>>> map = new HashMap<>();
        if (log.isDebugEnabled()) {
            log.debug("flat-message: {}", new String(bytes));
        }
        FlatMessage flatMessage = JSON.parseObject(bytes, FlatMessage.class);
        List<Map<String, String>> data = flatMessage.getData();
        // only dml supported
        if (!flatMessage.getIsDdl() && !data.isEmpty()) {
            ArrayList<CanalEntity<T>> entities = new ArrayList<>(data.size());
            processData(flatMessage, entities);
            map.put(flatMessage.getTable(), entities);
        }
        return map;
    }

    private <T> void processData(FlatMessage flatMessage, ArrayList<CanalEntity<T>> entities)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        List<Map<String, String>> datas = flatMessage.getData();
        List<Map<String, String>> olds = flatMessage.getOld();
        String type = flatMessage.getType();
        String table = flatMessage.getTable();
        Class<T> tableClass = canalEntryHandlerFactory.getTableClass(table);
        int length = datas.size();
        for (int i = 0; i < length; i++) {
            CanalEntity<T> canalEntity = new CanalEntity<>();
            canalEntity.setTableClass(tableClass);
            canalEntity.setTableName(table);
            canalEntity.setType(CanalEntry.EventType.valueOf(type));
            final Map<String, String> data = datas.get(i);
            switch (type) {
                case "INSERT" -> {
                    canalEntity.setAfter(mapToEntity(table, tableClass, data));
                    entities.add(canalEntity);
                }
                case "UPDATE" -> {
                    Map<String, String> old = olds.get(i);
                    // FlatMessage中old只包含字段值变更的字段，其值为变更前数据
                    copyUnChangedPropertyToOld(data, old);
                    canalEntity.setBefore(mapToEntity(table, tableClass, old));
                    canalEntity.setAfter(mapToEntity(table, tableClass, data));
                    entities.add(canalEntity);
                }
                case "DELETE" -> {
                    canalEntity.setBefore(mapToEntity(table, tableClass, data));
                    entities.add(canalEntity);
                }
            }
        }
    }

    private void copyUnChangedPropertyToOld(Map<String, String> data, Map<String, String> old) {
        Set<String> keySet = new HashSet<>(old.keySet());
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (!keySet.contains(entry.getKey())) {
                old.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private <T> T mapToEntity(String tableName, Class<T> tableClass, Map<String, String> map)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        final Map<String, Field> tableField = canalEntryHandlerFactory.getTableField(tableName);
        T instance = tableClass.getDeclaredConstructor()
                .newInstance();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            Field field = tableField.get(entry.getKey());
            if (field != null) {
                field.setAccessible(true);
                field.set(instance, CanalEntryMapper.convertType(field.getType(), entry.getValue()));
            }
        }
        return instance;
    }
}
