package top.wang3.hami.security.ratelimit.handler;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;

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

    private RedisScript<List<Long>> redisScript;


    @PostConstruct
    private void loadScript() {
        redisScript = RedisClient.loadScript("/META-INF/scripts/slide_window.lua", Long.class);
    }

    @Override
    public RateLimit.Algorithm getSupportedAlgorithm() {
        return RateLimit.Algorithm.SLIDE_WINDOW;
    }

    @Override
    public List<Long> execute(String key, double rate, double capacity) {
        long current = Instant.now().getEpochSecond();
        List<String> keys = Arrays.asList(key, String.valueOf(System.currentTimeMillis()));
        //fix 传入的参数不应为List
        //fix objectMapper 配置ObjectMapper.DefaultTyping.EVERYTHING导致long类型序列化错误
        return RedisClient.executeScript(redisScript, keys, List.of(rate, capacity, current));
    }
}
