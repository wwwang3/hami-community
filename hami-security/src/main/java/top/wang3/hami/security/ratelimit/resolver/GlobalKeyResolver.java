package top.wang3.hami.security.ratelimit.resolver;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import top.wang3.hami.security.ratelimit.annotation.KeyMeta;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;

@Component
public class GlobalKeyResolver implements RateLimitKeyResolver {


    @Override
    public RateLimit.Scope getScope() {
        return RateLimit.Scope.GLOBAL;
    }

    @Override
    public String resolve(@NonNull KeyMeta keyMeta) {
        return RateLimit.Scope.GLOBAL.toString();
    }
}
