package top.wang3.hami.security.ratelimit.annotation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 限流属性
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RateLimiterModel {

    private RateLimit.Algorithm algorithm;
    private RateLimit.Scope scope;
    private RateMeta rateMeta;

    /**
     * 用于生成Key的一些参数
     */
    private KeyMeta keyMeta;

    /**
     * 错误消息
     */
    private String blockMsg;

    public RateLimiterModel(RateLimit.Algorithm algorithm, RateLimit.Scope scope, RateMeta rateMeta, KeyMeta keyMeta) {
        this.algorithm = algorithm;
        this.scope = scope;
        this.rateMeta = rateMeta;
        this.keyMeta = keyMeta;
    }
}
