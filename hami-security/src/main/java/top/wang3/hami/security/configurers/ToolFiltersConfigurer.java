package top.wang3.hami.security.configurers;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import top.wang3.hami.common.component.SnowflakeIdGenerator;
import top.wang3.hami.security.filter.IpContextFilter;
import top.wang3.hami.security.filter.RequestIDFilter;
import top.wang3.hami.security.filter.RequestLogFilter;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class ToolFiltersConfigurer extends AbstractHttpConfigurer<ToolFiltersConfigurer, HttpSecurity> {

    boolean enableRequestID;
    boolean enableRequestLog;

    List<RequestMatcher> ignoreLogUrlMatchers = new ArrayList<>();

    @Override
    public void configure(HttpSecurity builder) {
        IpContextFilter ipContextFilter = new IpContextFilter();
        builder.addFilterBefore(ipContextFilter, WebAsyncManagerIntegrationFilter.class);
        if (enableRequestID) {
            RequestIDFilter requestIDFilter = new RequestIDFilter();
            requestIDFilter.setGenerator(new SnowflakeIdGenerator());
            builder.addFilterAfter(requestIDFilter, IpContextFilter.class);
        }
        if (enableRequestLog) {
            RequestLogFilter requestLogFilter = new RequestLogFilter();
            requestLogFilter.setIgnoreLogUrls(ignoreLogUrlMatchers);
            builder.addFilterBefore(requestLogFilter, LogoutFilter.class);
        }
    }

    public ToolFiltersConfigurer enableRequestID(boolean enable) {
        enableRequestID = enable;
        return this;
    }

    public ToolFiltersConfigurer enableRequestLog(boolean enable) {
        enableRequestLog = enable;
        return this;
    }

    public ToolFiltersConfigurer addIgnoreLogUrl(String pattern) {
        if (StringUtils.hasText(pattern)) {
            ignoreLogUrlMatchers.add(new AntPathRequestMatcher(pattern, null, true, null));
        }
        return this;
    }

    public ToolFiltersConfigurer addIgnoreLogUrl(String ...patterns) {
        if (patterns != null) {
            for (String pattern : patterns) {
                ignoreLogUrlMatchers.add(new AntPathRequestMatcher(pattern, null, true, null));
            }
        }
        return this;
    }

    public static ToolFiltersConfigurer create() {
        return new ToolFiltersConfigurer();
    }

    public static Customizer<ToolFiltersConfigurer> withDefaults() {
        return conf -> conf
                .enableRequestID(true)
                .enableRequestLog(true)
                .addIgnoreLogUrl("/swagger-ui", "/v3/api-docs", "/favicon.ico");
    }
}
