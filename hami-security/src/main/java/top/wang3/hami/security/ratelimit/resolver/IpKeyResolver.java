package top.wang3.hami.security.ratelimit.resolver;

import org.springframework.stereotype.Component;
import top.wang3.hami.security.model.RateLimiterModel;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;

@Component
public class IpKeyResolver implements RateLimitKeyResolver {


    @Override
    public RateLimit.Scope getScope() {
        return RateLimit.Scope.IP;
    }

    @Override
    public String resolve(RateLimiterModel model) {
        if (model == null) throw new IllegalArgumentException("null");
        return model.getIp() == null ? "unknown" : model.getIp();
    }
}
