package top.wang3.hami.security.ratelimit.resolver;

import org.springframework.lang.NonNull;
import top.wang3.hami.security.ratelimit.annotation.KeyMeta;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;

public interface RateLimitKeyResolver {


    RateLimit.Scope getScope();

    String resolve(@NonNull KeyMeta keyMeta);
}
