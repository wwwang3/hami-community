package top.wang3.hami.security.annotation;

import org.springframework.context.ApplicationContext;
import org.springframework.security.access.hierarchicalroles.NullRoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authorization.AuthorityAuthorizationDecision;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public enum AccessControl {

    /**
     * 禁止访问
     */
    DENY((r, s, a) -> r.requestMatchers(s.httpMethod, s.patterns).denyAll()),

    /**
     * 必须包含全部角色,角色不能以ROLE_开头
     */
    ROLES((r, s, a) -> {
        List<String> roles = Arrays.stream(s.roles).map(i -> "ROLE_" + i).toList();
        r.requestMatchers(s.httpMethod, s.patterns).access(new CustomAuthoritiesAuthorizationManager(a, roles));
    }),

    /**
     * 必须包含所有权限
     */
    AUTHORITIES((r, s, a) -> {
        List<String> authorities = List.of(s.authorities);
        r.requestMatchers(s.httpMethod, s.patterns).access(new CustomAuthoritiesAuthorizationManager(a, authorities));
    }),

    /**
     * 任意一个角色
     */
    ANY_ROLE((r, s, a) -> r.requestMatchers(s.httpMethod, s.patterns).hasAnyRole(s.roles)),

    /**
     * 任意一个权限
     */
    ANY_AUTHORITY((r, s, a) -> r.requestMatchers(s.httpMethod, s.patterns).hasAnyAuthority(s.authorities)),


    /**
     * 有认证成功的authentication
     */
    AUTHENTICATED((r, s, a) -> r.requestMatchers(s.httpMethod, s.patterns).authenticated()),

    /**
     * 匿名authentication
     */
    ANONYMOUS((r, s, a) -> r.requestMatchers(s.httpMethod, s.patterns).anonymous()),

    /**
     * 公共API authenticated 或者 anonymous 或者 empty
     */
    PUBLIC((r, s, a) -> r.requestMatchers(s.httpMethod, s.patterns).permitAll());


    final TFunction<
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry,
            ApiInfo,
            ApplicationContext> configurer;

    AccessControl(TFunction<
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry,
            ApiInfo,
            ApplicationContext> configurer) {
        this.configurer = configurer;
    }

    public void apply(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry conf, ApiInfo info, ApplicationContext context) {
        configurer.apply(conf, info, context);
    }

    static class CustomAuthoritiesAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

        private final RoleHierarchy roleHierarchy;
        private final Collection<String> authorities;

        CustomAuthoritiesAuthorizationManager(ApplicationContext applicationContext, Collection<String> authorities) {
            this.roleHierarchy = (applicationContext.getBeanNamesForType(RoleHierarchy.class).length > 0)
                    ? applicationContext.getBean(RoleHierarchy.class) : new NullRoleHierarchy();
            this.authorities = authorities;
        }


        @Override
        public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
            Authentication auth = authentication.get();
            Collection<? extends GrantedAuthority> grantedAuthorities = roleHierarchy.getReachableGrantedAuthorities(auth.getAuthorities());
            Set<String> collected = grantedAuthorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
            boolean granted = auth.isAuthenticated() && collected.containsAll(authorities);
            return new AuthorityAuthorizationDecision(granted, AuthorityUtils.createAuthorityList(authorities));
        }
    }

}
