package top.wang3.hami.security.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import top.wang3.hami.security.filter.RateLimitFilter;
import top.wang3.hami.security.model.WebSecurityProperties;
import top.wang3.hami.security.ratelimit.RateLimiter;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;

@Configuration
@ComponentScan(basePackages = {"top.wang3.hami.security.ratelimit"})
@ConditionalOnProperty(value = "hami.security.rate-limit.enable")
@Slf4j
public class RateLimitConfig {

    private final WebSecurityProperties properties;

    public RateLimitConfig(WebSecurityProperties properties) {
        this.properties = properties;
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimiterFilter(RateLimiter rateLimiter,
                                                                     RequestMappingHandlerMapping requestMappingHandlerMapping) {
        WebSecurityProperties.RateLimitFilterProperties config = properties.getRateLimit();
        RateLimitFilter filter = new RateLimitFilter();
        filter.setRateLimiter(rateLimiter);
        //IP
        filter.setScope(RateLimit.Scope.IP);
        filter.setCapacity(config.getCapacity());
        filter.setRate(config.getRate());
        filter.setAlgorithm(config.getAlgorithm());
        filter.setRequestMappingHandlerMapping(requestMappingHandlerMapping);
        var bean = new FilterRegistrationBean<>(filter);
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
        log.debug("rate-limiter-filter configured for use: {}", config);
        bean.addUrlPatterns("/*");
        return bean;
    }

}
