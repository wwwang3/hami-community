package top.wang3.hami.canal;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

@SuppressWarnings(value = {"unchecked", "rawtypes"})
public class CanalEntryMapper {

    private static final String[] PARSE_PATTERNS = new String[]{"yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm", "yyyy-MM", "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss",
            "yyyy/MM/dd HH:mm", "yyyy/MM", "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss",
            "yyyy.MM.dd HH:mm", "yyyy.MM", }; //fix: Date类型解析失败, 导致NPE

    public static Object convertType(Class<?> type, String columnValue) throws JsonProcessingException {
        if (!StringUtils.hasText(columnValue)) {
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
        } else if (type.equals(Byte.class)) {
            return Byte.parseByte(columnValue);
        } else if (type.equals(Date.class)) {
            return parseDate(columnValue);
        } else {
            return JSON.parseObject(columnValue, type);
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

}
