package top.wang3.hami.core;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;

@ConfigurationProperties(prefix = "hami")
@Data
public class HamiProperties {

    String version;

    @NestedConfigurationProperty
    Init init;

    @Data
    public static class Init {
        boolean enable = false;
        List<String> list;
    }
}
