package top.wang3.hami.security.ratelimit;

import org.springframework.stereotype.Component;
import top.wang3.hami.security.model.RateLimiterModel;

@Component
public class GlobalKeyResolver implements RateLimitKeyResolver {


    @Override
    public String getScope() {
        return "global";
    }

    @Override
    public String resolve(RateLimiterModel model) {
        return "global";
    }
}
