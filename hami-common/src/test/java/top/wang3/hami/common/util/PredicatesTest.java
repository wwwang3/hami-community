package top.wang3.hami.common.util;

import org.junit.jupiter.api.Test;

class PredicatesTest {
    @Test
    void testChainCheck() {
        Predicates.check(RandomUtils.randomInt(200) > 45)
                .then(() -> {
                    System.out.println(1145);
                    long l = RandomUtils.randomLong(1, 200);
                    System.out.println(l);
                    return l > 20;
                })
                .end(System.out::println);
    }
}