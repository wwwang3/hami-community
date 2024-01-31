package top.wang3.hami.security.configurers;

import org.springframework.context.ApplicationContext;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import top.wang3.hami.security.filter.IpContextFilter;
import top.wang3.hami.security.filter.RateLimitFilter;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;

@SuppressWarnings("unused")
public class GlobalRateLimitFilterConfigurer extends
        AbstractHttpConfigurer<GlobalRateLimitFilterConfigurer, HttpSecurity> {

    private final RateLimitFilter filter = new RateLimitFilter();

    @Override
    public void init(HttpSecurity http) throws Exception {
        ApplicationContext context = http.getSharedObject(ApplicationContext.class);
        filter.setApplicationContext(context);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        filter.afterPropertiesSet();
        http.addFilterAfter(filter, IpContextFilter.class);
    }

    public GlobalRateLimitFilterConfigurer algorithm(RateLimit.Algorithm algorithm) {
        filter.setAlgorithm(algorithm);
        return this;
    }

    public GlobalRateLimitFilterConfigurer rate(double rate) {
        filter.setRate(rate);
        return this;
    }

    public GlobalRateLimitFilterConfigurer capacity(double capacity) {
        filter.setCapacity(capacity);
        return this;
    }

    public static GlobalRateLimitFilterConfigurer create() {
        return new GlobalRateLimitFilterConfigurer();
    }

    public static Customizer<GlobalRateLimitFilterConfigurer> withDefaults() {
        return conf -> {
          conf.rate(100);
          conf.capacity(1000);
          conf.algorithm(RateLimit.Algorithm.SLIDE_WINDOW);
        };
    }
}
