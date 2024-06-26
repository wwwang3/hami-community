package top.wang3.hami.security.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import top.wang3.hami.common.constant.Constants;

import java.util.concurrent.TimeUnit;

/**
 * redis黑名单实现
 */
@Slf4j
public class RedisBlackListStorage implements BlacklistStorage {

    public static final String BLACK_LIST_PREFIX = "blacklist:";

    StringRedisTemplate redisTemplate;

    public RedisBlackListStorage(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean add(String jwtId, long expireAt) {
        long remain = expireAt - System.currentTimeMillis();
        // 已经过期了
        if (remain <= 0) return true;
        redisTemplate.opsForValue().set(BLACK_LIST_PREFIX + jwtId, Constants.EMPTY_STRING,
                remain, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public boolean contains(String jwtId) {
        return redisTemplate.opsForValue().get(BLACK_LIST_PREFIX + jwtId) != null;
    }
}
