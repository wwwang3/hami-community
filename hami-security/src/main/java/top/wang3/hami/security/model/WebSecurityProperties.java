package top.wang3.hami.security.model;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;

import java.util.List;


@ConfigurationProperties(prefix = "hami.security")
@Data
public class WebSecurityProperties {

    /**
     * jwt秘钥
     */
    private String secret;

    /**
     * token名称 (从cookie或者header中读取token时cookie或者header的名称)
     */
    private String tokenName = "access_token";

    /**
     * token有效期 单位s
     * 默认7天
     */
    private int expire = 604800;

    @NestedConfigurationProperty
    private CookieConfig cookie = new CookieConfig();

    /**
     * 表单登录接口
     */
    private String formLoginApi = "/api/v1/auth/login";

    /**
     * 表单登录的用户名参数
     */
    private String usernameParameter = "account";

    /**
     * 表单登录的密码参数
     */
    private String passwordParameter = "password";

    /**
     * 退出登录接口
     */
    private String logoutApi = "/api/v1/auth/logout";

    /**
     * 不需要登录访问的接口
     */
    private String[] allowedApis;

    /**
     * 允许跨域的站点
     */
    private List<String> allowedOrigins;

    @NestedConfigurationProperty
    private RateLimitFilterConfig rateLimit;

    @Data
    public static class CookieConfig {

        /**
         * 是否将token写入cookie, 读取时会从header或者cookie中寻找
         */
        boolean enable = true;

        /**
         * http only
         */
        boolean httpOnly = true;

        /**
         * cookie写入的域名
         */
        String domain;

        /**
         * cookie path
         */
        String path = "/";
    }


    /**
     * 全局限流过滤器配置
     *
     */
    @Data
    public static class RateLimitFilterConfig {

        boolean enable;

        /**
         * 限流算法
         */
        RateLimit.Algorithm algorithm = RateLimit.Algorithm.SLIDE_WINDOW;

        /**
         * 应用范围, GLOBAL, IP, URI
         */
        RateLimit.Scope scope = RateLimit.Scope.IP;

        /**
         * 请求速率
         */
        int rate = 10;

        /**
         * 最大容量
         */
       int capacity = 200;
    }
}
