package top.wang3.hami.security.ratelimit;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.security.annotation.RateLimit;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * 滑动窗口实现
 */
@Slf4j
@Component
@SuppressWarnings(value = {"rawtypes", "unchecked"})
public class SlideWindowRateLimiterHandler implements RateLimiterHandler {

    private RedisScript<Long> redisScript;


    @PostConstruct
    private void loadScript() {
        redisScript = RedisClient.loadScript("/META-INF/scripts/slide_window.lua");
    }

    @Override
    public String getSupportedAlgorithm() {
        return RateLimit.Algorithm.SLIDE_WINDOW.getName();
    }

    @Override
    public boolean isAllowed(String key, int rate, int capacity) {
        long s = System.currentTimeMillis();
        long current = Instant.now().getEpochSecond();
        List<String> keys = Arrays.asList(key, String.valueOf(System.currentTimeMillis()));
        //fix 传入的参数不应为List
        //fix objectMapper 配置ObjectMapper.DefaultTyping.EVERYTHING导致long类型序列化错误
        Long allowed = RedisClient.executeScript(redisScript, keys, List.of(rate, capacity, current));
        long e = System.currentTimeMillis();
        System.out.println(e - s);
        return allowed != null && allowed ==  1L;
    }

}
