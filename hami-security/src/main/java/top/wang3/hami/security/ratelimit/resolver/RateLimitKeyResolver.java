package top.wang3.hami.security.ratelimit.resolver;

import top.wang3.hami.security.model.RateLimiterModel;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;

public interface RateLimitKeyResolver {


    RateLimit.Scope getScope();

    String resolve(RateLimiterModel model);
}
