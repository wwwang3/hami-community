package top.wang3.hami.core;


import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.util.StringUtils;
import top.wang3.hami.core.init.InitializerEnums;

import java.util.List;
import java.util.regex.Pattern;

@ConfigurationProperties(prefix = "hami")
@Data
public class HamiProperties implements InitializingBean {

    public static final Pattern EMAIL_REGEX = Pattern.compile("^([-_A-Za-z0-9.]+)@([_A-Za-z0-9]+\\.)+[A-Za-z0-9]{2,3}$");

    String version;

    /**
     * 工作目录
     */
    String workDir;

    @NestedConfigurationProperty
    Init init;

    String email;

    @NestedConfigurationProperty
    Log log;

    /**
     * 是否开启CostLog注解切面, 打印方法执行时间日志
     */
    Boolean costLog;

    @Override
    public void afterPropertiesSet() {
        if (!StringUtils.hasText(email) || !EMAIL_REGEX.matcher(email).matches()) {
            throw new IllegalArgumentException("valid email address");
        }
    }

    @Data
    public static class Init {
        boolean enable = false;
        List<InitializerEnums> list;
    }

    @Data
    public static class Log {
        String path;
        String filename;
    }
}
