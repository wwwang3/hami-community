package top.wang3.hami.security.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.Ordered;
import top.wang3.hami.security.filter.RateLimiterFilter;
import top.wang3.hami.security.model.WebSecurityProperties;
import top.wang3.hami.security.ratelimit.RateLimiter;

@Configuration
@ComponentScan(basePackages = {"top.wang3.hami.security.ratelimit", "top.wang3.hami.security.aspect"})
@ConditionalOnProperty(value = "hami.security.rate-limit.enable")
@EnableAspectJAutoProxy
@Slf4j
public class RateLimitConfig {

    private final WebSecurityProperties properties;

    public RateLimitConfig(WebSecurityProperties properties) {
        this.properties = properties;
    }

    @Bean
    public FilterRegistrationBean<RateLimiterFilter> rateLimiterFilter(RateLimiter rateLimiter) {
        WebSecurityProperties.RateLimitConfig config = properties.getRateLimit();
        RateLimiterFilter filter = new RateLimiterFilter();
        filter.setRateLimiter(rateLimiter);
        filter.setScope(config.getScope());
        filter.setCapacity(config.getCapacity());
        filter.setRate(config.getRate());
        filter.setAlgorithm(config.getAlgorithm());
        var bean = new FilterRegistrationBean<>(filter);
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
        log.debug("rate-limiter-filter configured for use: {}", config);
        bean.addUrlPatterns("/*");
        return bean;
    }

}
