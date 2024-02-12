package top.wang3.hami.common.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class DateUtils {


    public static final DateTimeFormatter NORMAL_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault()).withZone(ZoneId.systemDefault());

    public static final DateTimeFormatter NORMAL_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).withZone(ZoneId.systemDefault());

    public static final DateTimeFormatter FULL_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).withZone(ZoneId.systemDefault());

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

    public static long plusHours(Date date, long hours) {
        return ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
                .plusHours(hours)
                .toInstant()
                .toEpochMilli();
    }

    public static long minusHours(Date date, long hours) {
        return ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
                .minusHours(hours)
                .toInstant()
                .toEpochMilli();
    }

    public static Date randomDate(int startYear, int endYear) {
        int year = RandomUtils.randomInt(startYear, endYear);
        int month = RandomUtils.randomInt(1, 12);
        int day = getDayOfMonth(year, month);
        int hour = RandomUtils.randomInt(0, 23);
        int minute = RandomUtils.randomInt(0, 59);
        int second = RandomUtils.randomInt(0, 59);
        int mills = RandomUtils.randomInt(0, 999);
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.YEAR, year);
        instance.set(Calendar.MONTH, month);
        instance.set(Calendar.DAY_OF_MONTH, day);
        instance.set(Calendar.HOUR, hour);
        instance.set(Calendar.MINUTE, minute);
        instance.set(Calendar.SECOND, second);
        instance.set(Calendar.MILLISECOND, mills);
        return instance.getTime();
    }

    public static boolean isLoopYear(int year) {
        return ((year & 3) == 0) && ((year % 100) != 0 || (year % 400) == 0);
    }

    private static int getDayOfMonth(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("The month range is [1,12].");
        } else if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
            return 31;
        } else if (month == 4 || month == 6 || month == 9 || month == 11) {
            return 30;
        } else if (isLoopYear(year)) {
            return 29;
        } else {
            return 28;
        }
    }

    public static String formatDate(Date date) {
        return formatDate(date.getTime());
    }

    public static String formatDate(long mills) {
        return NORMAL_DATE_FORMATTER.format(LocalDate.ofInstant(
                Instant.ofEpochMilli(mills),
                ZoneId.systemDefault())
        );
    }

    public static String formatDateTime(Date date) {
        return formatDateTime(date.getTime());
    }

    public static String formatDate(TemporalAccessor date) {
        return NORMAL_DATE_FORMATTER.format(date);
    }

    public static String normalFormatDatetime(TemporalAccessor datetime) {
        return NORMAL_DATE_TIME_FORMATTER.format(datetime);
    }

    public static String formatDateTime(TemporalAccessor dateTime) {
        return FULL_DATE_FORMATTER.format(dateTime);
    }

    public static String formatDateTime(long mills) {
        return FULL_DATE_FORMATTER.format(LocalDateTime.ofInstant(
                Instant.ofEpochMilli(mills),
                ZoneId.systemDefault())
        );
    }

}
