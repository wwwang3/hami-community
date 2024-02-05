package top.wang3.hami.security.configurers;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter;
import top.wang3.hami.common.component.SnowflakeIdGenerator;
import top.wang3.hami.security.filter.IpContextFilter;
import top.wang3.hami.security.filter.RequestIDFilter;
import top.wang3.hami.security.filter.RequestLogFilter;

@SuppressWarnings("UnusedReturnValue")
public class ToolFiltersConfigurer extends AbstractHttpConfigurer<ToolFiltersConfigurer, HttpSecurity> {

    boolean enableRequestID;
    boolean enableRequestLog;
    @Override
    public void configure(HttpSecurity builder) {
        IpContextFilter ipContextFilter = new IpContextFilter();
        RequestIDFilter requestIDFilter = new RequestIDFilter();
        requestIDFilter.setGenerator(new SnowflakeIdGenerator());
        RequestLogFilter requestLogFilter = new RequestLogFilter();
        builder.addFilterBefore(ipContextFilter, WebAsyncManagerIntegrationFilter.class);
        if (enableRequestID) {
            builder.addFilterAfter(requestIDFilter, IpContextFilter.class);
        }
        if (enableRequestLog) {
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

    public static ToolFiltersConfigurer create() {
        return new ToolFiltersConfigurer();
    }

    public static Customizer<ToolFiltersConfigurer> withDefaults() {
        return conf -> conf.enableRequestID(true).enableRequestLog(true);
    }
}
