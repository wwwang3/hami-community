package top.wang3.hami.security.configurers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.AbstractRequestMatcherRegistry;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcherEntry;
import org.springframework.util.StringUtils;
import top.wang3.hami.security.filter.RateLimitFilter;
import top.wang3.hami.security.filter.RequestLogFilter;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;
import top.wang3.hami.security.ratelimit.annotation.RateLimitModel;
import top.wang3.hami.security.ratelimit.annotation.RateMeta;

import java.util.ArrayList;
import java.util.List;

@Getter
@Slf4j
@SuppressWarnings("unused")
public class GlobalRateLimitFilterConfigurer extends
        AbstractHttpConfigurer<GlobalRateLimitFilterConfigurer, HttpSecurity> {

    private final ApiRateLimitRegistry registry;

    public GlobalRateLimitFilterConfigurer(HttpSecurity http) {
        this.registry = new ApiRateLimitRegistry(http.getSharedObject(ApplicationContext.class));
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        RateLimitFilter rateLimitFilter = new RateLimitFilter();
        List<RequestMatcherEntry<RateLimitModel>> requestMatcherEntries = registry.getRequestMatcherEntries();
        rateLimitFilter.setRequestMatcherEntries(requestMatcherEntries);
        rateLimitFilter.setApplicationContext(http.getSharedObject(ApplicationContext.class));
        rateLimitFilter.afterPropertiesSet();
        http.addFilterAfter(rateLimitFilter, RequestLogFilter.class);
        if (log.isDebugEnabled()) {
            StringBuilder builder = new StringBuilder();
            int i = 0;
            int size = requestMatcherEntries.size();
            builder.append('{');
            for (RequestMatcherEntry<RateLimitModel> entry : requestMatcherEntries) {
                builder.append("[matcher: ");
                RequestMatcher matcher = entry.getRequestMatcher();
                builder.append(matcher.toString());
                builder.append(", model: ");
                RateLimitModel rateLimitModel = entry.getEntry();
                builder.append(rateLimitModel.toString());
                i++;
                if (i < size) {
                    builder.append("], ");
                } else {
                    builder.append("]");
                }
            }
            builder.append("}");
            log.debug("add {} filter-rate-limit config. \nconfig: {}", requestMatcherEntries.size(),
                    builder);
        }
    }

    private ApiRateLimitRegistry addApiRateLimit(List<? extends RequestMatcher> requestMatchers, RateLimitModel rateLimit) {
        for (RequestMatcher requestMatcher : requestMatchers) {
            this.registry.addRateLimit(requestMatcher, rateLimit);
        }
        return this.registry;
    }

    @SuppressWarnings("UnusedReturnValue")
    public final class ApiRateLimitRegistry extends AbstractRequestMatcherRegistry<ApiRateLimitConfig> {

        private final List<RequestMatcherEntry<RateLimitModel>> requestMatcherEntries = new ArrayList<>();

        public static final String BLOCK_MSG = "大哥别刷了( ´･･)ﾉ(._.`)";

        public ApiRateLimitRegistry(ApplicationContext applicationContext) {
            super();
            super.setApplicationContext(applicationContext);
        }

        private List<RequestMatcherEntry<RateLimitModel>> getRequestMatcherEntries() {
            return this.requestMatcherEntries;
        }

        @Override
        protected ApiRateLimitConfig chainRequestMatchers(List<RequestMatcher> requestMatchers) {
            return new ApiRateLimitConfig(requestMatchers);
        }

        private ApiRateLimitRegistry addRateLimit(RequestMatcher matcher, RateLimitModel rateLimit) {
            requestMatcherEntries.add(new RequestMatcherEntry<>(matcher, rateLimit));
            return this;
        }

    }

    public final class ApiRateLimitConfig {
        private final List<? extends RequestMatcher> requestMatchers;
        private final RateLimitModel model;

        public ApiRateLimitConfig(List<? extends RequestMatcher> requestMatchers) {
            this.requestMatchers = requestMatchers;
            this.model = new RateLimitModel();
        }

        public ApiRateLimitConfig algorithm(RateLimit.Algorithm algorithm) {
            model.setAlgorithm(algorithm);
            return this;
        }

        public ApiRateLimitConfig scope(RateLimit.Scope scope) {
            model.setScope(scope);
            return this;
        }

        public ApiRateLimitConfig create(RateLimit.Algorithm algorithm, RateLimit.Scope scope) {
            this.algorithm(algorithm);
            this.scope(scope);
            return this;
        }

        public ApiRateLimitConfig blockMsg(String blockMsg) {
            if (StringUtils.hasText(blockMsg)) {
                model.setBlockMsg(blockMsg);
            }
            return this;
        }

        public ApiRateLimitRegistry build(RateMeta rateMeta) {
            model.setRateMeta(rateMeta);
            return GlobalRateLimitFilterConfigurer.this.addApiRateLimit(requestMatchers, model);
        }

    }
}
