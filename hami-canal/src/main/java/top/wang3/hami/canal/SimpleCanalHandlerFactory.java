package top.wang3.hami.canal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.google.common.base.CaseFormat;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@SuppressWarnings(value = {"rawtypes", "unchecked"})
public class SimpleCanalHandlerFactory implements CanalEntryHandlerFactory, ApplicationRunner {

    /**
     * handler未指定容器ID时, 放到这里
     */
    private final Map<String, List<CanalEntryHandler<?>>> commonHandlerMap = new ConcurrentHashMap<>();

    /**
     * handler指定了容器ID时, 放到这里
     */
    private final Map<String, List<CanalEntryHandler<?>>> containeredHandlerMap = new ConcurrentHashMap<>();

    private final Map<String, Class<?>> tableClassMap = new ConcurrentHashMap<>();

    private final Map<Class<? extends CanalEntryHandler>, Class<?>> handlerEntityClassMap = new ConcurrentHashMap<>();

    /**
     * tableClass字段缓存
     */
    private final Map<String, Map<String, Field>> tableFiledMap = new ConcurrentHashMap<>(32);

    private int handlerSize = 0;

    @Override
    public <T> Class<T> getTableClass(String tableName) {
        return (Class<T>) tableClassMap.get(tableName);
    }

    @Override
    public Class<?> getHandlerEntityCalss(CanalEntryHandler<?> handler) {
        return handlerEntityClassMap.get(handler.getClass());
    }

    @Override
    public List<CanalEntryHandler<?>> getContainerHandler(String containerId, String tableName) {
        String key = containerId + "-" + tableName;
        return containeredHandlerMap.get(key);
    }

    @Override
    public List<CanalEntryHandler<?>> getHandler(String tableName) {
        return commonHandlerMap.get(tableName);
    }

    @Override
    public Map<String, Field> getTableField(String tableName) {
        return tableFiledMap.get(tableName);
    }

    @Override
    public boolean addTableClass(@NonNull String tableName, Class<?> tableClass) {
        tableClassMap.putIfAbsent(tableName, tableClass);
        addToTableClassFiledMap(tableName, tableClass);
        return true;
    }

    @Override
    public boolean addHandlerEntityClass(Class<? extends CanalEntryHandler> handlerClass, Class<?> tableClass) {
        handlerEntityClassMap.putIfAbsent(handlerClass, tableClass);
        return true;
    }

    @Override
    public boolean addCanalEntryHandler(@NonNull String tableName, String containerId, CanalEntryHandler<?> handler) {
        if (StringUtils.hasText(containerId)) {
            return addToContaineredHandlerMap(tableName, containerId, handler);
        } else {
            return addToCommonHandlerMap(tableName, handler);
        }
    }

    private void addToTableClassFiledMap(String tableName, Class<?> tableClass) {
        Field[] fields = tableClass.getDeclaredFields();
        if (tableFiledMap.containsKey(tableName)) {
            return;
        }
        Map<String, Field> map = Arrays.stream(fields)
                .collect(Collectors.toMap(this::getColumnName, Function.identity()));
        tableFiledMap.put(tableName, map);
    }

    private boolean addToCommonHandlerMap(String tableName, CanalEntryHandler<?> handler) {
        List<CanalEntryHandler<?>> handlers = commonHandlerMap.computeIfAbsent(tableName, key -> new ArrayList<>());
        handlerSize++;
        return handlers.add(handler);
    }

    private boolean addToContaineredHandlerMap(String tableName, String containerId, CanalEntryHandler<?> handler) {
        String key = containerId + "-" + tableName;
        List<CanalEntryHandler<?>> handlers = containeredHandlerMap.computeIfAbsent(key, k -> new ArrayList<>());
        handlerSize++;
        return handlers.add(handler);
    }

    private String getColumnName(Field field) {
        TableId tableId = field.getAnnotation(TableId.class);
        if (tableId != null) {
            return resolveName(tableId.value());
        }
        TableField tableField = field.getAnnotation(TableField.class);
        if (tableField != null) {
            return resolveName(tableField.value());
        }
        //驼峰转下划线
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());
    }

    private String resolveName(String name) {
        //兼容反引号
        if (name.charAt(0) == '`' && name.charAt(name.length() -1) == '`') {
            return name.substring(1, name.length() - 1);
        }
        return name;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("found {} CanalEntryHandler", handlerSize);
    }
}
