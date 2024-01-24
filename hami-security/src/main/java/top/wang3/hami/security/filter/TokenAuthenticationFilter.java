package top.wang3.hami.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import top.wang3.hami.security.model.LoginUser;
import top.wang3.hami.security.service.TokenService;

import java.io.IOException;

/**
 * token认证过滤器
 */
@Slf4j
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    private final String tokenName;

    public TokenAuthenticationFilter(TokenService tokenService, String tokenName) {
        this.tokenService = tokenService;
        this.tokenName = tokenName;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = tokenService.getToken(request, tokenName);
        LoginUser loginUser = tokenService.resolveToken(token);
        if (loginUser != null) {
            var authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            // 将认证成功的token写入Security上下文
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            log.debug("authentication success, LoginUser: {}", loginUser);
        }
        // 继续过滤器链
        filterChain.doFilter(request, response);
    }
}
