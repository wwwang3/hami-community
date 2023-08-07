package top.wang3.hami.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import top.wang3.hami.security.model.LoginUser;
import top.wang3.hami.security.service.TokenService;

/**
 * todo
 */
@Slf4j
public class AuthenticationPostHandler {

    private final TokenService tokenService;

    public AuthenticationPostHandler(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    public void handleSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        String token = tokenService.createToken(loginUser);
    }

    public void handleError(HttpServletRequest request, HttpServletResponse response, Exception e) {
        if (e instanceof AccessDeniedException ae) {
            //todo
        } else if (e instanceof AuthenticationException ae) {
            //todo
        }

    }


}
