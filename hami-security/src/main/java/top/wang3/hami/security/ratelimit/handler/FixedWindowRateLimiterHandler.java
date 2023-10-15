package top.wang3.hami.security.ratelimit.handler;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;

import java.util.List;


@Component
@Slf4j
public class FixedWindowRateLimiterHandler implements RateLimiterHandler {

    private RedisScript<List<Long>> fixedWindowScript;

    @PostConstruct
    public void init() {
        fixedWindowScript = RedisClient.loadScript("/META-INF/scripts/fixed_window.lua", Long.class);
    }

    @Override
    public RateLimit.Algorithm getSupportedAlgorithm() {
        return RateLimit.Algorithm.FIXED_WINDOW;
    }

    @Override
    public List<Long> execute(String key, double rate, double capacity) {
        long interval =  (long) (capacity / rate);
        return RedisClient.executeScript(fixedWindowScript, List.of(key), List.of(interval, capacity));
    }
}
