package top.wang3.hami.security.handler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import top.wang3.hami.security.config.WebSecurityProperties;
import top.wang3.hami.security.model.LoginUser;
import top.wang3.hami.security.model.Result;
import top.wang3.hami.security.service.TokenService;

import java.io.IOException;
import java.util.Map;

/**
 * todo
 */
@Slf4j
public class AuthenticationPostHandler {

    private final TokenService tokenService;
    private final WebSecurityProperties properties;
    public AuthenticationPostHandler(TokenService tokenService, WebSecurityProperties properties) {
        this.tokenService = tokenService;
        this.properties = properties;
    }

    public void handleSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        String token = tokenService.createToken(loginUser);
        String tokenName = properties.getTokenName();
        if (properties.getCookie().isEnable()) {
            writeCookie(response, token, loginUser);
        }
        //响应结果
        Map<String, String> map = Map.of(tokenName, token);
        Result<Map<String, String>> result = Result.success(map);

        writeResponse(response, result);

    }

    public void handleError(HttpServletRequest request, HttpServletResponse response, Exception e) {
        log.debug("login failed: error_class: {}, error_msg: {}", e.getClass().getName(), e.getMessage());
        Result<?> result;
        if (e instanceof AccessDeniedException ae) {
            //todo
            result = Result.error(403, ae.getMessage());
        } else if (e instanceof AuthenticationException ae) {
            //todo
            result = Result.error(400, ae.getMessage());
        }
        writeResponse(response, result);
    }

    private void writeCookie(HttpServletResponse response, String token, LoginUser loginUser) {
        WebSecurityProperties.CookieConfig config = properties.getCookie();
        Cookie cookie = new Cookie(properties.getTokenName(), token);
        cookie.setDomain(config.getDomain());
        cookie.setPath(config.getPath());
        cookie.setHttpOnly(config.isHttpOnly());
        cookie.setMaxAge((int) (loginUser.getExpireAt().getTime() / 1000));
        response.addCookie(cookie);
    }

    private <T> void writeResponse(HttpServletResponse response, Result<T> result) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        try {
            response.getWriter().write(result.toJsonString());
        } catch (IOException e) {
            log.error("write response failed: error_class: {}, error_msg: {}",
                    e.getClass().getName(), e.getMessage());
        }
    }

}
