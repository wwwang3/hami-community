package top.wang3.hami.security.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;

/**
 * 限流属性
 */
@Data
@Builder
@AllArgsConstructor
public class RateLimiterModel {

    private RateLimit.Algorithm algorithm;
    private RateLimit.Scope scope;
    private double capacity;
    private double rate;

    /**
     * 方法名 过滤器中使用时为空
     */
    private String methodName;

    /**
     * 类名 过滤器中使用时为空
     */
    private String className;

    /**
     * 请求URI
     */
    private String uri;

    /**
     * 请求IP
     */
    private String ip;

}
