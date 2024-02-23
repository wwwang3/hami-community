package top.wang3.hami.security.ratelimit.resolver;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.wang3.hami.security.ratelimit.annotation.KeyMeta;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;

@Component
public class UriKeyResolver implements RateLimitKeyResolver {
    @Override
    public RateLimit.Scope getScope() {
        return RateLimit.Scope.URI;
    }

    @Override
    public String resolve(@NonNull KeyMeta keyMeta) {
        String uri = keyMeta.getUri();
        return  StringUtils.hasText(uri) ? uri : RateLimit.Scope.GLOBAL.toString();
    }
}
