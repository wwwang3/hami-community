package top.wang3.hami.common.util;

import java.util.concurrent.ThreadLocalRandom;

public final class RandomUtils {


    public static int getRandom(int length) {
        if (length < 1 || length > 9) throw new IllegalArgumentException("1 <= length <= 9");
        int start = (int) Math.pow(10, length - 1);
        int bound = (int) Math.pow(10, length);
        return ThreadLocalRandom.current()
                .nextInt(start, bound);
    }

    public static String randomIntStr(int length) {
        if (length < 1) {
            length = 1;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(randomInt(10));
        }
        return builder.toString();
    }

    public static long randomLong(long min, long max) {
        return ThreadLocalRandom.current()
                .nextLong(min, max + 1);
    }

    public static int randomInt(int bound) {
        return ThreadLocalRandom.current().nextInt(bound);
    }

}
