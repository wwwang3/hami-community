package top.wang3.hami.security.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 限流属性
 */
@Data
@Builder
@AllArgsConstructor
public class RateLimiterModel {

    private String algorithm;
    private String scope;
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
