package top.wang3.hami.security.configurers;

import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.util.CollectionUtils;
import top.wang3.hami.security.annotation.ApiInfo;
import top.wang3.hami.security.annotation.provider.ApiInfoProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @deprecated use {@link AuthorizeConfigurerCustomizer}
 */
@Deprecated
public class AuthorizeConfigurer extends AbstractHttpConfigurer<AuthorizeConfigurer, HttpSecurity> {

    private List<ApiInfoProvider> providers;

    private boolean anyRequest;

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

    /**
     * 自定义配置HttpSecurity
     * 使用这个configurer, 就不能继续在httpSecurity上配置anyRequest了
     * 因为HttpSecurity#authorizeHttpRequests()中的anyRequest配置了后就不能配置其他的了，
     * 而HttpSecurity#authorizeHttpRequests()配置会优先于此配置执行, 后续执行此configurer就会报错
     * 所以要么在这个配置里面配置anyRequest, 要么不配置
     * 或者依照{@link  AuthorizeHttpRequestsConfigurer#configure(HttpSecurityBuilder)}再创建一个新的Filter
     * 推荐使用 {@link AuthorizeConfigurerCustomizer}配置authorizeHttpRequests(), 且能较好控制配置顺序
     *
     * @param http HttpSecurity对象
     * @throws Exception Exception
     */
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
                // fix: anyRequest和自定义API访问控制配置的顺序问题
                // 内部error转发请求
                registry.requestMatchers("/error").permitAll();
                // favicon.ico
                registry.requestMatchers("/favicon.ico").permitAll();
                if (anyRequest) {
                    applyDefaults(registry);
                }
            });
        }
    }

    private void applyDefaults(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        registry.anyRequest().authenticated();
    }

    public AuthorizeConfigurer providers(List<ApiInfoProvider> providers) {
        this.providers = providers;
        return this;
    }

    public AuthorizeConfigurer anyRequest(boolean configAnyRequest) {
        this.anyRequest = configAnyRequest;
        return this;
    }

    public static AuthorizeConfigurer create() {
        return new AuthorizeConfigurer();
    }


}
