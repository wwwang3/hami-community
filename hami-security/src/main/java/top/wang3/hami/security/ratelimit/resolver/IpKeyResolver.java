package top.wang3.hami.security.ratelimit.resolver;

import org.springframework.stereotype.Component;
import top.wang3.hami.common.dto.IpInfo;
import top.wang3.hami.security.ratelimit.annotation.KeyMeta;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;

@Component
public class IpKeyResolver implements RateLimitKeyResolver {


    @Override
    public RateLimit.Scope getScope() {
        return RateLimit.Scope.IP;
    }

    @Override
    public String resolve(KeyMeta keyMeta) {
        String ip = keyMeta.getIp();
        return ip == null ? IpInfo.UNKNOWN_IP : ip;
    }
}
