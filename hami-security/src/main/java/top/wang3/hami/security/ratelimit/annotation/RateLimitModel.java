package top.wang3.hami.security.ratelimit.annotation;

import lombok.Data;

/**
 * 限流属性
 */
@Data
public class RateLimitModel {

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
    private String blockMsg = "大哥别刷了( ´･･)ﾉ(._.`)";

}
