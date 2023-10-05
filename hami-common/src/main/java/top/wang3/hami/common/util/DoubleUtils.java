package top.wang3.hami.common.util;

import java.util.Date;

public class DoubleUtils {


    public static Double dateToDouble(Date date) {
        return (double) date.getTime();
    }
}
