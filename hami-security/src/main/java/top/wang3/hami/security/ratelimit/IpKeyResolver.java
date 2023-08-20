package top.wang3.hami.security.ratelimit;

import org.springframework.stereotype.Component;
import top.wang3.hami.security.model.RateLimiterModel;

@Component
public class IpKeyResolver implements RateLimitKeyResolver {


    @Override
    public String getScope() {
        return "ip";
    }

    @Override
    public String resolve(RateLimiterModel model) {
        if (model == null) throw new IllegalArgumentException("null");
        return model.getIp() == null ? "unknown" : model.getIp();
    }
}
