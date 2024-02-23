package top.wang3.hami.security.ratelimit.resolver;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import top.wang3.hami.security.ratelimit.annotation.KeyMeta;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;

@Component
public class MethodKeyResolver implements RateLimitKeyResolver {

    @Override
    public RateLimit.Scope getScope() {
        return RateLimit.Scope.METHOD;
    }

    @Override
    public String resolve(@NonNull KeyMeta keyMeta) {
        return keyMeta.getClassName() + "#" + keyMeta.getMethodName();
    }
}
