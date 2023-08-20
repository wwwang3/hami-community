package top.wang3.hami.security.config;


import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import top.wang3.hami.security.filter.IpContextFilter;
import top.wang3.hami.security.filter.RequestTimeDebugFilter;

@Configuration
public class FilterBeanConfig {

    @Bean
    public FilterRegistrationBean<IpContextFilter> ipContextFilter() {
        var bean = new FilterRegistrationBean<IpContextFilter>();
        bean.setFilter(new IpContextFilter());
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        bean.addUrlPatterns("/*");
        return bean;
    }

    @Profile(value = "dev")
    @Bean
    public FilterRegistrationBean<RequestTimeDebugFilter> requestTimeDebugFilterBean() {
        var bean = new FilterRegistrationBean<>(new RequestTimeDebugFilter());
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        bean.addUrlPatterns("/*");
        return bean;
    }
}
