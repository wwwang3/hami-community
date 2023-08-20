package top.wang3.hami.security.ratelimit;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
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
    private final RedisTemplate redisTemplate;

    @Autowired
    public SlideWindowRateLimiterHandler(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    private void loadScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        String path = "/META-INF/scripts/slide_window.lua";
        ResourceScriptSource source = new ResourceScriptSource(new ClassPathResource(path));
        script.setScriptSource(source);
        script.setResultType(Long.class);
        this.redisScript = script;
        log.debug("success load lua script: {}", path);
    }

    @Override
    public String getSupportedAlgorithm() {
        return RateLimit.Algorithm.SLIDE_WINDOW.getName();
    }

    @Override
    public boolean isAllowed(String key, int rate, int capacity) {
        log.debug("rate: {}, capacity: {}", rate, capacity);
        long current = Instant.now().getEpochSecond();
        List<String> keys = Arrays.asList(key, String.valueOf(System.currentTimeMillis()));
        List<String> args = Arrays.asList(String.valueOf(rate), String.valueOf(capacity), String.valueOf(current));
        Long allowed = (Long) redisTemplate.execute(redisScript, keys, args);
        return allowed != null && allowed ==  1L;
    }

}
