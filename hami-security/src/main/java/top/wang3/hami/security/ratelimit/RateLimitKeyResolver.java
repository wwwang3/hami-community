package top.wang3.hami.security.ratelimit;

import top.wang3.hami.security.model.RateLimiterModel;

public interface RateLimitKeyResolver {


    String getScope();

    String resolve(RateLimiterModel model);
}
