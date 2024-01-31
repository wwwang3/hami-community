package top.wang3.hami.security.configurers;

import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.security.annotation.ApiInfo;
import top.wang3.hami.security.annotation.provider.ApiInfoProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class AuthorizeConfigurer extends AbstractHttpConfigurer<AuthorizeConfigurer, HttpSecurity> {

    private List<ApiInfoProvider> providers;

    public AuthorizeConfigurer() {
    }

    @Override
    public void init(HttpSecurity http) throws Exception {
        ApplicationContext context = http.getSharedObject(ApplicationContext.class);
        Map<String, ApiInfoProvider> providerMap = context.getBeansOfType(ApiInfoProvider.class);
        if (!providerMap.isEmpty()) {
            providers = new ArrayList<>(providerMap.values());
        }
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        ApplicationContext context = http.getSharedObject(ApplicationContext.class);
        if (!CollectionUtils.isEmpty(providers)) {
            http.authorizeHttpRequests(registry -> {
                // 接口访问配置
                for (ApiInfoProvider provider : providers) {
                    List<ApiInfo> apiList = provider.getApis();
                    if (!CollectionUtils.isEmpty(apiList)) {
                        for (ApiInfo info : apiList) {
                            info.getAccessControl().apply(registry, info, context);
                        }
                    }
                }
            });
        }
    }

    public AuthorizeConfigurer providers(List<ApiInfoProvider> providers) {
        this.providers = providers;
        return this;
    }

    public static AuthorizeConfigurer create() {
        return new AuthorizeConfigurer();
    }


}
