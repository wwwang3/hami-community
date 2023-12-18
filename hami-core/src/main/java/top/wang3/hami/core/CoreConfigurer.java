package top.wang3.hami.core;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
        "top.wang3.hami.core.config",
        "top.wang3.hami.core.service",
        "top.wang3.hami.core.component",
        "top.wang3.hami.core.job",
        "top.wang3.hami.core.aspect",
        "top.wang3.hami.core.init",
})
public class CoreConfigurer {
}
