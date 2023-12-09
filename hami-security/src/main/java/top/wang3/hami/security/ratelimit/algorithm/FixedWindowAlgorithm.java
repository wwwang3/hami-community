package top.wang3.hami.security.ratelimit.algorithm;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;
import top.wang3.hami.security.ratelimit.annotation.RateMeta;

import java.util.List;


@Component
@Slf4j
public class FixedWindowAlgorithm implements RateLimiterAlgorithm {

    private RedisScript<List<Long>> fixedWindowScript;

    @PostConstruct
    public void init() {
        fixedWindowScript = RedisClient.loadScript("/META-INF/scripts/fixed_window.lua", Long.class);
    }

    @Override
    public RateLimit.Algorithm getName() {
        return RateLimit.Algorithm.FIXED_WINDOW;
    }

    @Override
    public List<Long> execute(String key, RateMeta rateMeta) {
        List<? extends  Number> args = List.of(rateMeta.getInterval(), rateMeta.getCapacity());
        return RedisClient.executeScript(fixedWindowScript, List.of(key), args);
    }
}
