package top.wang3.hami.security.storage;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * redis黑名单实现
 */
public class RedisBlackListStorage implements BlacklistStorage {

    public static final String BLACK_LIST_PREFIX = "blacklist:";

    RedisTemplate<String, Object> redisTemplate;

    public RedisBlackListStorage(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean add(String jwtId, long expireAt) {
        long remain = expireAt - System.currentTimeMillis();
        //已经过期了
        if (remain <= 0) return true;
        redisTemplate.opsForValue().set(BLACK_LIST_PREFIX + jwtId, "",
                remain, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public boolean contains(String jwtId) {
        return redisTemplate.opsForValue().get(BLACK_LIST_PREFIX + jwtId) != null;
    }
}
