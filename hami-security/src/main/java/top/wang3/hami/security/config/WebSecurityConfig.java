package top.wang3.hami.security.config;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import top.wang3.hami.security.configurers.AuthorizeConfigurerCustomizer;
import top.wang3.hami.security.configurers.GlobalRateLimitFilterConfigurer;
import top.wang3.hami.security.configurers.TokenAuthenticationConfigurer;
import top.wang3.hami.security.configurers.ToolFiltersConfigurer;
import top.wang3.hami.security.context.TtlSecurityContextHolderStrategy;
import top.wang3.hami.security.handler.AuthenticationEventHandler;
import top.wang3.hami.security.handler.AuthenticationPostHandler;
import top.wang3.hami.security.handler.DefaultAuthenticationPostHandler;
import top.wang3.hami.security.listener.AuthenticationEventListener;
import top.wang3.hami.security.model.WebSecurityProperties;
import top.wang3.hami.security.service.TokenService;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(WebSecurityProperties.class)
@EnableWebSecurity
@Import(value = {JwtTokenServiceConfig.class})
@ComponentScan(basePackages = {"top.wang3.hami.security.annotation.provider", "top.wang3.hami.security.ratelimit"})
@Slf4j
public class WebSecurityConfig {

    private final WebSecurityProperties properties;

    public WebSecurityConfig(WebSecurityProperties properties) {
        this.properties = properties;
    }


    @PostConstruct
    private void setSecurityStrategy() {
        SecurityContextHolder.setStrategyName(TtlSecurityContextHolderStrategy.class.getName());
        log.debug("security-strategy: {}", SecurityContextHolder.getContextHolderStrategy().getClass().getSimpleName());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnBean(AuthenticationEventHandler.class)
    public AuthenticationEventPublisher authenticationEventPublisher() {
        return new DefaultAuthenticationEventPublisher();
    }

    @Bean(initMethod = "init")
    @ConditionalOnBean(AuthenticationEventHandler.class)
    public AuthenticationEventListener authenticationEventListener(AuthenticationEventHandler handler) {
        return new AuthenticationEventListener(handler);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationPostHandler authenticationPostHandler(TokenService tokenService, WebSecurityProperties properties) {
        return new DefaultAuthenticationPostHandler(tokenService, properties);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationPostHandler handler) throws Exception {
        return http
                // 自定义接口访问配置
                .authorizeHttpRequests(AuthorizeConfigurerCustomizer.build(http))
                .authorizeHttpRequests(conf -> {
                    // 默认接口访问配置
                    conf.requestMatchers("/error").permitAll();
                    conf.requestMatchers("/favicon.ico").permitAll();
                    conf.anyRequest().authenticated();
                })
                // csrf配置
                .csrf(CsrfConfigurer::disable)
                .cors(conf -> {
                    // 跨域配置
                    CorsConfiguration cors = new CorsConfiguration();
                    cors.setAllowedOrigins(properties.getAllowedOrigins());
                    cors.setAllowCredentials(true);
                    cors.addAllowedHeader("*");
                    cors.addAllowedMethod("*");
                    cors.addExposedHeader("*");
                    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                    source.registerCorsConfiguration("/**", cors);
                    conf.configurationSource(source);
                })
                .exceptionHandling(conf -> {
                    // 异常处理
                    conf.accessDeniedHandler(handler::handleError);
                    conf.authenticationEntryPoint(handler::handleError);
                })
                .formLogin(conf -> {
                    // 表单登录
                    // @formatter:off
                    conf
                        .usernameParameter(properties.getUsernameParameter())
                        .passwordParameter(properties.getPasswordParameter())
                        .loginProcessingUrl(properties.getFormLoginApi())
                        .permitAll()
                        .successHandler(handler::handleLoginSuccess)
                        .failureHandler(handler::handleError);
                })
                .logout(conf -> {
                    // 退出登录
                    // @formatter:off
                    conf
                        .logoutUrl(properties.getLogoutApi())
                        .logoutSuccessHandler(handler::handleLogoutSuccess);
                })
                // Token认证器
                .with(TokenAuthenticationConfigurer.create(), this::applyTokenConfig)
                // 请求ID, 请求日志, IPContext
                .with(ToolFiltersConfigurer.create(), ToolFiltersConfigurer.withDefaults())
                // 全局IP限流
                .with(GlobalRateLimitFilterConfigurer.create(), this::applyRateLimitConfig)
                .sessionManagement(conf -> conf.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    private void applyTokenConfig(TokenAuthenticationConfigurer configurer) {
        configurer.tokenName(properties.getTokenName());
    }

    private void applyRateLimitConfig(GlobalRateLimitFilterConfigurer conf) {
        if (!properties.getRateLimit().isEnable()) {
            conf.disable();
            return;
        }
        // @formatter:off
        conf.rate(properties.getRateLimit().getRate())
            .capacity(properties.getRateLimit().getCapacity())
            .algorithm(properties.getRateLimit().getAlgorithm());
    }

}
