package top.wang3.hami.common.util;

import java.util.Random;

public final class RandomUtils {


    public static int getRandom(int length) {
        if (length < 1 || length > 9) throw new IllegalArgumentException("1 <= length <= 9");
        int start = (int) Math.pow(10, length - 1);
        int bound = (int) Math.pow(10, length);
        return new Random().nextInt(start, bound);
    }

}
