package top.wang3.hami.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import top.wang3.hami.common.component.Ip2RegionSearcher;
import top.wang3.hami.common.component.Ip2RegionSearcherImpl;

@Configuration
public class HamiCommonConfig {

    @Bean
    public Ip2RegionSearcher ip2RegionSearcher(@Autowired ResourceLoader resourceLoader) {
        return new Ip2RegionSearcherImpl(resourceLoader);
    }
}
