package top.wang3.hami.security.ratelimit.algorithm;

import top.wang3.hami.security.ratelimit.annotation.RateLimit;
import top.wang3.hami.security.ratelimit.annotation.RateMeta;

import java.util.List;

public interface RateLimiterAlgorithm {


    RateLimit.Algorithm getName();

    List<Long> execute(String key, RateMeta rateMeta);
}
