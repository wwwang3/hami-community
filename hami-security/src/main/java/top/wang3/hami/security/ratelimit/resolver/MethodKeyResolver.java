package top.wang3.hami.security.ratelimit.resolver;

import org.springframework.stereotype.Component;
import top.wang3.hami.security.model.RateLimiterModel;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;

@Component
public class MethodKeyResolver implements RateLimitKeyResolver {

    @Override
    public RateLimit.Scope getScope() {
        return RateLimit.Scope.METHOD;
    }

    @Override
    public String resolve(RateLimiterModel model) {
        return model.getClassName() + "#" + model.getMethodName();
    }
}
