package top.wang3.hami.security.configurers;

import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import top.wang3.hami.security.filter.TokenAuthenticationFilter;
import top.wang3.hami.security.service.TokenService;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class TokenAuthenticationConfigurer
        extends AbstractHttpConfigurer<TokenAuthenticationConfigurer, HttpSecurity> {

    private TokenService tokenService;
    private String tokenName;

    @Override
    public void init(HttpSecurity http) throws Exception {
        super.init(http);
        initDefaultTokenFilter(http);
    }

    private void initDefaultTokenFilter(HttpSecurity http) {
        this.tokenName = "access_token";
        this.tokenService = http.getSharedObject(ApplicationContext.class).getBean(TokenService.class);
    }

    @Override
    public void configure(HttpSecurity http) {
        TokenAuthenticationFilter tokenAuthenticationFilter = new TokenAuthenticationFilter(tokenService, tokenName);
        // 添加token过滤器
        http.addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    public TokenAuthenticationConfigurer tokenService(TokenService tokenService) {
        this.tokenService = tokenService;
        return this;
    }

    public TokenAuthenticationConfigurer tokenName(String tokenName) {
        this.tokenName = tokenName;
        return this;
    }

    public static TokenAuthenticationConfigurer create() {
        return new TokenAuthenticationConfigurer();
    }


}
