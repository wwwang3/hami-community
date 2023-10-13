package top.wang3.hami.security.ratelimit.resolver;

import org.springframework.stereotype.Component;
import top.wang3.hami.security.context.LoginUserContext;
import top.wang3.hami.security.model.RateLimiterModel;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;

@Component
public class LoginUserKeyResolver implements RateLimitKeyResolver {

    @Override
    public RateLimit.Scope getScope() {
        return RateLimit.Scope.LOGIN_USER;
    }

    @Override
    public String resolve(RateLimiterModel model) {
        //没有报错
        return String.valueOf(LoginUserContext.getLoginUserId());
    }
}
