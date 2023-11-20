package top.wang3.hami.security.config;


import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import top.wang3.hami.common.component.SnowflakeIdGenerator;
import top.wang3.hami.security.filter.IpContextFilter;
import top.wang3.hami.security.filter.RequestIDFilter;

@Configuration
public class FilterBeanConfig {

    @Bean
    public FilterRegistrationBean<IpContextFilter> ipContextFilter() {
        var bean = new FilterRegistrationBean<IpContextFilter>();
        bean.setFilter(new IpContextFilter());
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        bean.addUrlPatterns("/api/v1/*");
        return bean;
    }

    @Bean
    public FilterRegistrationBean<RequestIDFilter> requestIDFilter(SnowflakeIdGenerator generator) {
        RequestIDFilter filter = new RequestIDFilter();
        filter.setGenerator(generator);
        FilterRegistrationBean<RequestIDFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(filter);
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registrationBean.addUrlPatterns("/api/v1/*");
        return registrationBean;
    }
}
