package top.wang3.hami.common.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public final class DateUtils {


    public static final DateTimeFormatter NORMAL_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter FULL_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");


    public static long offsetMonths(Date date, long offset) {
        return offset < 0 ? minusMonths(date, offset) : plusMonths(date, offset);
    }

    public static long plusMonths(Date date, long monthsToAdd) {
        return ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
                .plusMonths(monthsToAdd)
                .toInstant()
                .toEpochMilli();
    }

    public static long minusMonths(Date date, long months) {
        return ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
                .minusMonths(months)
                .toInstant()
                .toEpochMilli();
    }

    public static String formatDate(Date date) {
        return formatDate(date.getTime());
    }

    public static String formatDateTime(Date date) {
        return formatDateTime(date.getTime());
    }

    public static String formatDate(long mills) {
        return NORMAL_DATE_FORMATTER.format(LocalDate.ofInstant(
                Instant.ofEpochMilli(mills),
                ZoneId.systemDefault())
        );
    }

    public static String formatDateTime(long mills) {
        return FULL_DATE_FORMATTER.format(LocalDateTime.ofInstant(
                Instant.ofEpochMilli(mills),
                ZoneId.systemDefault())
        );
    }

}
