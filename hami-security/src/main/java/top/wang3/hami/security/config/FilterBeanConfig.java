package top.wang3.hami.security.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import top.wang3.hami.security.filter.LoginUserContextFilter;
import top.wang3.hami.security.filter.RequestTimeDebugFilter;
import top.wang3.hami.security.filter.TokenAuthenticationFilter;
import top.wang3.hami.security.service.TokenService;

@Configuration
public class FilterBeanConfig {

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter(TokenService tokenService,
                                                               @Value("${hami.security.tokenName}") String tokenName) {
        return new TokenAuthenticationFilter(tokenService, tokenName);
    }

    @Bean
    public LoginUserContextFilter loginUserContextFilter() {
        return new LoginUserContextFilter();
    }

    @Profile(value = "dev")
    @Bean
    FilterRegistrationBean<RequestTimeDebugFilter> requestTimeDebugFilterBean() {
        var bean = new FilterRegistrationBean<>(new RequestTimeDebugFilter());
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        bean.addUrlPatterns("/*");
        return bean;
    }
}
