package top.wang3.hami.security.ratelimit;

import top.wang3.hami.security.annotation.RateLimit;

public interface RateLimiterHandler {

    boolean support(RateLimit.Algorithm algorithm);

    boolean isAllowed(String key, int rate, int capacity);
}
