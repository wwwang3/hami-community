package top.wang3.hami.security.config;


import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import top.wang3.hami.security.filter.IpContextFilter;
import top.wang3.hami.security.filter.RequestLogFilter;

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

//    @Bean
//    @Profile(value = "dev")
//    public FilterRegistrationBean<RequestTimeDebugFilter> requestTimeDebugFilter() {
//        var bean = new FilterRegistrationBean<>(new RequestTimeDebugFilter());
//        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
//        bean.addUrlPatterns("/*");
//        return bean;
//    }

    @Bean
    public FilterRegistrationBean<RequestLogFilter> requestLogFilter() {
        RequestLogFilter filter = new RequestLogFilter();
        FilterRegistrationBean<RequestLogFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(filter);
        //越小优先级越高
        //放置在spring-security过滤器之后
        bean.setOrder(SecurityProperties.DEFAULT_FILTER_ORDER  + 10);
        bean.addUrlPatterns("/api/v1/*");
        return bean;
    }
}
