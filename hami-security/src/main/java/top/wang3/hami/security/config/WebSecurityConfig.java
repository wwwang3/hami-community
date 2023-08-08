package top.wang3.hami.security.config;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import top.wang3.hami.security.handler.AuthenticationPostHandler;
import top.wang3.hami.security.service.TokenService;

@Configuration
@EnableConfigurationProperties(WebSecurityProperties.class)
@EnableWebSecurity
public class WebSecurityConfig {

    private final WebSecurityProperties properties;

    public WebSecurityConfig(WebSecurityProperties properties) {
        this.properties = properties;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, TokenService tokenService) throws Exception {
        //todo 完善配置
        AuthenticationPostHandler handler = new AuthenticationPostHandler(tokenService, properties);
        return http
                .authorizeHttpRequests(auth -> { //接口访问配置
                    String[] apis = properties.getAllowedApis();
                    auth.requestMatchers(apis).permitAll();
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
                .formLogin(conf -> {
                    conf
                            .loginProcessingUrl(properties.getFormLoginApi())
                            .successHandler(handler::handleSuccess)
                            .failureHandler(handler::handleError)
                            .permitAll();
                })
                .exceptionHandling(conf -> {
                    conf
                            .accessDeniedHandler(handler::handleError)
                            .authenticationEntryPoint(handler::handleError);
                })
                .build();
    }

}
