package top.wang3.hami.security.annotation.provider;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.wang3.hami.security.annotation.ApiInfo;
import top.wang3.hami.security.model.WebSecurityProperties;

import java.util.List;

/**
 * 从配置文件中获取ApiInfo
 */
@Component
@Order(1)
public class PropertiesApiInfoProvider implements ApiInfoProvider {

    private final WebSecurityProperties properties;

    public PropertiesApiInfoProvider(WebSecurityProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<ApiInfo> getApis() {
        return properties.getApiInfos();
    }

}
