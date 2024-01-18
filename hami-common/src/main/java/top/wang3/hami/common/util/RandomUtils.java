package top.wang3.hami.common.util;

import java.util.concurrent.ThreadLocalRandom;

public final class RandomUtils {

    public static String randomIntStr(int length) {
        if (length < 1) {
            length = 1;
        }
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(randomInt(10));
        }
        return builder.toString();
    }

    public static long randomLong(long min, long max) {
        return ThreadLocalRandom.current()
                .nextLong(min, max + 1);
    }

    /**
     * 随机整数, [0, bound)
     * @param bound bound
     * @return random num
     */
    public static int randomInt(int bound) {
        return ThreadLocalRandom.current().nextInt(bound);
    }

    /**
     * 随机整数, [start, end]
     * @param start start
     * @param end end
     * @return random num
     */
    public static int randomInt(int start, int end) {
        return ThreadLocalRandom.current().nextInt(start, end + 1);
    }

}
