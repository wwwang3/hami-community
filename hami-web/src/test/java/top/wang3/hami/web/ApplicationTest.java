package top.wang3.hami.web;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.wang3.hami.common.util.RedisClient;

import java.util.List;
import java.util.Map;

@SpringBootTest
public class ApplicationTest {

    @Test
    public void test1() {
        List<Map<String, Object>> maps = RedisClient.hMGetAll(List.of("#count:user:33", "#count:user:128", "test"), (key, index) -> {
            return (Map<String, Object>) null;
        });
        maps.forEach(m -> System.out.println(m));
    }
}
