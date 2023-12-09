package top.wang3.hami.security.ratelimit.resolver;

import org.springframework.stereotype.Component;
import top.wang3.hami.security.ratelimit.annotation.KeyMeta;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;

@Component
public class GlobalKeyResolver implements RateLimitKeyResolver {

    public static final String GLOBAL_KEY = "GLOBAL";

    @Override
    public RateLimit.Scope getScope() {
        return RateLimit.Scope.GLOBAL;
    }

    @Override
    public String resolve(KeyMeta keyMeta) {
        return GLOBAL_KEY;
    }
}
