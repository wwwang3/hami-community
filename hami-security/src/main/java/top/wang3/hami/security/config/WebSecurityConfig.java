package top.wang3.hami.security.config;


import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import top.wang3.hami.security.filter.LoginUserContextFilter;
import top.wang3.hami.security.filter.TokenAuthenticationFilter;
import top.wang3.hami.security.handler.AuthenticationEventHandler;
import top.wang3.hami.security.handler.AuthenticationPostHandler;
import top.wang3.hami.security.listener.AuthenticationEventListener;
import top.wang3.hami.security.service.TokenService;

@Configuration
@EnableConfigurationProperties(WebSecurityProperties.class)
@EnableWebSecurity
@Import(value = {JwtTokenServiceConfig.class, FilterBeanConfig.class})
@Slf4j
public class WebSecurityConfig {

    private final WebSecurityProperties properties;

    public WebSecurityConfig(WebSecurityProperties properties) {
        this.properties = properties;
    }

    @Resource
    private TokenAuthenticationFilter tokenFilter;

    @Resource
    private LoginUserContextFilter userFilter;

    @PostConstruct
    private void setSecurityStrategy() {
        SecurityContextHolder.setStrategyName("top.wang3.hami.security.context.TtlSecurityContextHolderStrategy");
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http, TokenService tokenService) throws Exception {
        log.debug("token-service: {}", tokenService.getClass().getSimpleName());
        AuthenticationPostHandler handler = new AuthenticationPostHandler(tokenService, properties);
        return http
                .authorizeHttpRequests(auth -> { //接口访问配置
                    String[] apis = properties.getAllowedApis();
                    if (apis != null) {
                        auth.requestMatchers(apis).permitAll();
                    }
                    auth.anyRequest().authenticated();
                })
                .csrf(CsrfConfigurer::disable) //csrf配置
                .cors(conf -> { //跨域配置
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
                .exceptionHandling(conf -> conf
                        .accessDeniedHandler(handler::handleError)
                        .authenticationEntryPoint(handler::handleError)
                )
                .formLogin(conf -> conf
                        .usernameParameter(properties.getUsernameParameter())
                        .passwordParameter(properties.getPasswordParameter())
                        .loginProcessingUrl(properties.getFormLoginApi())
                        .permitAll()
                        .successHandler(handler::handleLoginSuccess)
                        .failureHandler(handler::handleError)
                )
                .logout(conf -> conf
                        .logoutUrl(properties.getLogoutApi())
                        .logoutSuccessHandler(handler::handleLogoutSuccess)
                )
                .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(userFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(conf -> conf.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

}
