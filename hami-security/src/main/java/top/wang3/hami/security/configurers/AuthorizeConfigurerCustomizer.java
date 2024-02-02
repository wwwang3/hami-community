package top.wang3.hami.security.configurers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.security.annotation.ApiInfo;
import top.wang3.hami.security.annotation.provider.ApiInfoProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class AuthorizeConfigurerCustomizer {

    private List<ApiInfoProvider> providers;
    private ApplicationContext context;

    private AuthorizeConfigurerCustomizer(HttpSecurity http) {
        init(http);
    }

    public void init(HttpSecurity http) {
        ApplicationContext context = http.getSharedObject(ApplicationContext.class);
        this.context = context;
        Map<String, ApiInfoProvider> providerMap = context.getBeansOfType(ApiInfoProvider.class);
        if (!providerMap.isEmpty()) {
            this.providers = new ArrayList<>(providerMap.values());
        }
    }

    public static Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> build(HttpSecurity http) {
        return new AuthorizeConfigurerCustomizer(http).create();
    }

    public Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> create() {
        return registry -> {
            if (!CollectionUtils.isEmpty(providers)) {
                // 接口访问配置
                int size = 0;
                for (ApiInfoProvider provider : providers) {
                    List<ApiInfo> apiList = provider.getApis();
                    if (!CollectionUtils.isEmpty(apiList)) {
                        size += apiList.size();
                        for (ApiInfo info : apiList) {
                            info.getAccessControl().apply(registry, info, context);
                            log.debug("resoled api-access-control: {}", info);
                        }
                    }
                }
                log.debug("增加 {} 个接口访问配置.", size);
            }
        };
    }
}
