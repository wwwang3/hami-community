package top.wang3.hami.test;


import org.junit.jupiter.api.Test;
import top.wang3.hami.common.util.DateUtils;

import java.util.Date;

class UtilsTest {


    @Test
    void testDateUtils() {
        for (int i = 0; i < 100; i++) {
            long start = System.currentTimeMillis();
            long mills = DateUtils.plusMonths(new Date(), 3);
            long end = System.currentTimeMillis();
            System.out.println(end - start);
        }
    }

    @Test
    void testFormatDate() {
        for (int i = 0; i < 100; i++) {
            long start = System.currentTimeMillis();
            String s = DateUtils.formatDateTime(new Date());
            long end = System.currentTimeMillis();
            System.out.println(end - start);
            System.out.println(s);
        }
    }
}
