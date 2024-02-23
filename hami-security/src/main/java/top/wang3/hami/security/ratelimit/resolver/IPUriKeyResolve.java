package top.wang3.hami.security.ratelimit.resolver;

import org.springframework.stereotype.Component;
import top.wang3.hami.common.dto.IpInfo;
import top.wang3.hami.security.ratelimit.annotation.KeyMeta;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;

import java.util.Optional;

@Component
public class IPUriKeyResolve implements RateLimitKeyResolver {
    @Override
    public RateLimit.Scope getScope() {
        return RateLimit.Scope.IP_URI;
    }

    @Override
    public String resolve(KeyMeta keyMeta) {
        String ip = Optional.ofNullable(keyMeta.getIp()).orElse(IpInfo.UNKNOWN_IP);
        String uri = Optional.ofNullable(keyMeta.getUri()).orElse("empty_uri");
        return ip + uri;
    }
}
