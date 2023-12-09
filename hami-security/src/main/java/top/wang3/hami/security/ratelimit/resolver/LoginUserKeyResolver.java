package top.wang3.hami.security.ratelimit.resolver;

import org.springframework.stereotype.Component;
import top.wang3.hami.security.ratelimit.RateLimitException;
import top.wang3.hami.security.ratelimit.annotation.KeyMeta;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;

@Component
public class LoginUserKeyResolver implements RateLimitKeyResolver {

    @Override
    public RateLimit.Scope getScope() {
        return RateLimit.Scope.LOGIN_USER;
    }

    @Override
    public String resolve(KeyMeta keyMeta) {
        if (keyMeta.getLoginUserId() == null) {
            throw new RateLimitException("no login-user id found");
        }
        return keyMeta.getMethodName()+ ":" + keyMeta.getLoginUserId();
    }
}
