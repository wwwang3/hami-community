package top.wang3.hami.security.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;


@ConfigurationProperties(prefix = "hami.security")
@Data
public class WebSecurityProperties {

    /**
     * 默认jwt有效期 7天单位s
     */
    private static final long DEFAULT_TIMEOUT = 7 * 24 * 60 * 60;

    public static final String DEFAULT_JWT_NAME = "access_token";

    public static final String DEFAULT_FORM_LOGIN_API = "/api/v1/auth/login";


    /**
     * jwt秘钥
     */
    private String jwtSecret;

    /**
     * jwt名称 (从cookie或者header中读取jwt时cookie或者header的名称)
     */
    private String jwtName = DEFAULT_JWT_NAME ;

    /**
     * jwt有效期 单位s
     */
    private long jwtTimeout;


    private String formLoginApi = DEFAULT_FORM_LOGIN_API;

    /**
     * 不需要登录访问的接口
     */
    private String[] allowedApis;

    /**
     * 允许跨域的站点
     */
    private List<String> allowedOrigins;
}
