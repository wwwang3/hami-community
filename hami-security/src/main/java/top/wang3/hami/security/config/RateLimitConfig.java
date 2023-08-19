package top.wang3.hami.security.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"top.wang3.hami.security.ratelimit"})
public class RateLimitConfig {
}
