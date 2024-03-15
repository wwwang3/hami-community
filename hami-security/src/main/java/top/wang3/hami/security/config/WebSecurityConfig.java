package top.wang3.hami.security.config;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
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
import top.wang3.hami.security.handler.AuthenticationPostHandler;
import top.wang3.hami.security.handler.DefaultAuthenticationPostHandler;
import top.wang3.hami.security.model.WebSecurityProperties;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;
import top.wang3.hami.security.ratelimit.annotation.RateMeta;
import top.wang3.hami.security.service.TokenService;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(WebSecurityProperties.class)
@EnableWebSecurity
@EnableMethodSecurity
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
    @ConditionalOnMissingBean
    public AuthenticationPostHandler authenticationPostHandler(TokenService tokenService, WebSecurityProperties properties) {
        return new DefaultAuthenticationPostHandler(tokenService, properties);
    }

    @Bean
    @SuppressWarnings("all")
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationPostHandler handler) throws Exception {
        return http
            // 自定义接口访问配置
            .authorizeHttpRequests(AuthorizeConfigurerCustomizer.build(http))
            .authorizeHttpRequests(conf -> {
                // 默认接口访问配置
                conf.requestMatchers("/error").permitAll();
//                conf.requestMatchers("/favicon.ico").permitAll();
                conf.anyRequest().authenticated();
            })
            // csrf配置
            .csrf(CsrfConfigurer::disable)
            .cors(this::applyCorsConf)
            .exceptionHandling(conf -> {
                // 异常处理
                conf.accessDeniedHandler(handler::handleError);
                conf.authenticationEntryPoint(handler::handleError);
            })
            .formLogin(conf -> {
                // 表单登录
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
                conf
                    .logoutUrl(properties.getLogoutApi())
                    .logoutSuccessHandler(handler::handleLogoutSuccess);
            })
            /// 过滤器顺序 IP ==> 请求ID ==> Token ==> 请求日志 ==> 限流
            // Token认证器
            .with(TokenAuthenticationConfigurer.create(), this::applyTokenConfig)
            // IPContext, 请求ID, 请求日志
            .with(ToolFiltersConfigurer.create(), ToolFiltersConfigurer.withDefaults())
            // 全局IP限流
            .with(new GlobalRateLimitFilterConfigurer(http), this::applyRateLimitConfig)
            .sessionManagement(conf -> conf.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .build();
    }

    private void applyCorsConf(CorsConfigurer<HttpSecurity> conf) {
        // 跨域配置
        WebSecurityProperties.CorsConfig corsConfig = properties.getCors();
        if (log.isDebugEnabled()) {
            log.debug("cors-config: {}", corsConfig);
        }
        if (!Boolean.TRUE.equals(corsConfig.getEnable())) {
            conf.disable();
            return;
        }
        List<String> dep = properties.getAllowedOrigins();
        List<String> allowedOrigins = corsConfig.getAllowedOrigins();
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(allowedOrigins);
        if (dep != null && !dep.isEmpty()) {
            dep.forEach(cors::addAllowedOrigin);
        }
        cors.setAllowCredentials(corsConfig.getAllowCredentials());
        cors.setAllowedHeaders(corsConfig.getAllowedHeaders());
        cors.setAllowedMethods(corsConfig.getAllowedMethods());
        cors.setExposedHeaders(corsConfig.getExposeHeaders());
        cors.setMaxAge(corsConfig.getMaxAge());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(corsConfig.getPattern(), cors);
        conf.configurationSource(source);
    }


    private void applyTokenConfig(TokenAuthenticationConfigurer configurer) {
        configurer.tokenName(properties.getTokenName());
    }

    private void applyRateLimitConfig(GlobalRateLimitFilterConfigurer conf) {
        WebSecurityProperties.RateLimitFilterProperties rateLimit = properties.getRateLimit();
        if (rateLimit == null) return;
        if (!rateLimit.isEnable()) {
            conf.disable();
            return;
        }
        List<WebSecurityProperties.ApiRateLimitConfig> configs = rateLimit.getConfigs();
        if (configs != null && !configs.isEmpty()) {
            for (WebSecurityProperties.ApiRateLimitConfig config : configs) {
                String[] patterns = config.getPatterns();
                conf.getRegistry()
                    .requestMatchers(patterns)
                    .create(config.getAlgorithm(), config.getScope())
                    .blockMsg(config.getBlockMsg())
                    .build(config.getRateMeta());
            }
        }
        conf.getRegistry()
            .anyRequest()
            .create(RateLimit.Algorithm.SLIDE_WINDOW, RateLimit.Scope.IP)
            .build(new RateMeta(100, 6000));
    }

}
