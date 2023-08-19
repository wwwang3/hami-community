package top.wang3.hami.security.model;

import lombok.Data;
import top.wang3.hami.security.annotation.RateLimit;

/**
 * 限流属性
 */
@Data
public class RateLimiterModel {

    private RateLimit.Algorithm algorithm;
    private RateLimit.Scope scope;
    private int capacity;
    private int rate;

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
