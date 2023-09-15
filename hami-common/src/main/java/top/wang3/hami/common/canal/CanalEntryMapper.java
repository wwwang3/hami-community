package top.wang3.hami.common.canal;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CaseFormat;
import org.apache.commons.lang.time.DateUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings(value = {"unchecked", "rawtypes"})
public class CanalEntryMapper {

    public static final Map<Class<?>, Map<String, Field>> ENTRY_CACHE = new ConcurrentHashMap<>(32);

    private static final Map<Class<? extends CanalEntryHandler>, Class> ENTRY_TYPE_CACHE = new ConcurrentHashMap<>(16);

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 获取CanalEntry.Column和实体之间的映射关系
     */
    public static Map<String, Field> getFieldMap(Class<?> clazz) {
        Map<String, Field> map = ENTRY_CACHE.get(clazz);
        if (map == null) {
            Field[] fields = clazz.getDeclaredFields();
            map = Arrays.stream(fields)
                    .collect(Collectors.toMap(CanalEntryMapper::getColumnName, Function.identity()));
            ENTRY_CACHE.putIfAbsent(clazz, map);
        }
        return map;
    }

    private static String getColumnName(Field field) {
        TableId tableId = field.getAnnotation(TableId.class);
        if (tableId != null) {
            return tableId.value();
        }
        TableField tableField = field.getAnnotation(TableField.class);
        if (tableField != null) {
            return tableField.value();
        }
        //驼峰转下划线
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());
    }


    private static final String[] PARSE_PATTERNS = new String[]{"yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm", "yyyy-MM", "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss",
            "yyyy/MM/dd HH:mm", "yyyy/MM", "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss",
            "yyyy.MM.dd HH:mm", "yyyy.MM"};

    public static Object convertType(Class<?> type, String columnValue) throws JsonProcessingException {
        if (columnValue == null) {
            return null;
        } else if (type.equals(String.class)){
            return columnValue;
        } else if (type.equals(Integer.class)) {
            return Integer.parseInt(columnValue);
        } else if (type.equals(Long.class)) {
            return Long.parseLong(columnValue);
        } else if (type.equals(Boolean.class)) {
            return convertToBoolean(columnValue);
        } else if (type.equals(BigDecimal.class)) {
            return new BigDecimal(columnValue);
        } else if (type.equals(Double.class)) {
            return Double.parseDouble(columnValue);
        } else if (type.equals(Float.class)) {
            return Float.parseFloat(columnValue);
        } else if (type.equals(Date.class)) {
            return parseDate(columnValue);
        } else if (type.equals(Byte.class)) {
            return Byte.parseByte(columnValue);
        } else {
            return mapper.readValue(columnValue, type);
        }
    }

    private static Date parseDate(String str) {
        if (str == null) {
            return null;
        } else {
            try {
                return DateUtils.parseDate(str, PARSE_PATTERNS);
            } catch (ParseException var2) {
                return null;
            }
        }
    }

    private static boolean convertToBoolean(String value) {
        return "1".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> getTableClass(CanalEntryHandler object) {
        Class<? extends CanalEntryHandler> handlerClass = object.getClass();
        Class tableClass = ENTRY_TYPE_CACHE.get(handlerClass);
        if (tableClass == null) {
            Type[] interfacesTypes = handlerClass.getGenericInterfaces();
            for (Type t : interfacesTypes) {
                Class c = (Class) ((ParameterizedType) t).getRawType();
                if (c.equals(CanalEntryHandler.class)) {
                    tableClass = (Class<T>) ((ParameterizedType) t).getActualTypeArguments()[0];
                    ENTRY_TYPE_CACHE.putIfAbsent(handlerClass, tableClass);
                    return tableClass;
                }
            }
        }
        return tableClass;
    }

    public static  <T> T mapToEntity(List<CanalEntry.Column> columns, CanalEntryHandler handler) throws Exception {
        Class<T> clazz = getTableClass(handler);
        //must have no-args-constructor
        T t = clazz.getDeclaredConstructor()
                .newInstance();
        Map<String, Field> fieldMap = CanalEntryMapper.getFieldMap(clazz);
        for (CanalEntry.Column column : columns) {
            String name = column.getName();
            Field field = fieldMap.get(name);
            if (field != null) {
                field.setAccessible(true);
                field.set(t, CanalEntryMapper.convertType(field.getType(), column.getValue()));
            }
        }
        return t;
    }

    protected static void initEntryClassCache(CanalEntryHandler handler) {
        //init cache
        Class<?> clazz = getTableClass(handler);
        getFieldMap(clazz);
    }
}
