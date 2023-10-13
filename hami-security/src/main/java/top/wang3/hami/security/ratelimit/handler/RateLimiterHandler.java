package top.wang3.hami.security.ratelimit.handler;

import top.wang3.hami.security.ratelimit.annotation.RateLimit;

public interface RateLimiterHandler {


    RateLimit.Algorithm getSupportedAlgorithm();

    boolean isAllowed(String key, double rate, double capacity);
}
