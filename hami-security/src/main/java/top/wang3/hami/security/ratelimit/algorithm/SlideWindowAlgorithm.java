package top.wang3.hami.security.ratelimit.algorithm;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;
import top.wang3.hami.security.ratelimit.annotation.RateMeta;

import java.time.Instant;
import java.util.List;

/**
 * 滑动窗口实现
 */
@Slf4j
@Component
public class SlideWindowAlgorithm implements RateLimiterAlgorithm {

    private RedisScript<List<Long>> redisScript;

    @PostConstruct
    public void init() {
        redisScript = RedisClient.loadScript("scripts/slide_window.lua", Long.class);
    }

    @Override
    public RateLimit.Algorithm getName() {
        return RateLimit.Algorithm.SLIDE_WINDOW;
    }

    @Override
    public List<Long> execute(String key, RateMeta rateMeta) {
        long current = Instant.now().getEpochSecond();
        long member = System.currentTimeMillis();
        List<String> keys = List.of(key, String.valueOf(member));
        List<? extends Number> args = List.of(rateMeta.getRate(), rateMeta.getCapacity(), current);
        return RedisClient.executeScript(redisScript, keys, args);
    }
}
