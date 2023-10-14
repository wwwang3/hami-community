package top.wang3.hami.security.ratelimit.handler;

import top.wang3.hami.security.ratelimit.annotation.RateLimit;

import java.util.List;

public interface RateLimiterHandler {


    RateLimit.Algorithm getSupportedAlgorithm();

    List<Long> execute(String key, double rate, double capacity);
}
