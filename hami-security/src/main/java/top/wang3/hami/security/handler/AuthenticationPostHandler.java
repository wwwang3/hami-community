package top.wang3.hami.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import top.wang3.hami.security.model.LoginUser;
import top.wang3.hami.security.model.Result;
import top.wang3.hami.security.model.WebSecurityProperties;
import top.wang3.hami.security.service.TokenService;

import java.io.IOException;
import java.util.Map;

/**
 * 认证后置处理器, 处理登录成功, 失败, 退出登录
 */
@Slf4j
public class AuthenticationPostHandler {

    private final TokenService tokenService;
    private final WebSecurityProperties properties;
    public AuthenticationPostHandler(TokenService tokenService, WebSecurityProperties properties) {
        this.tokenService = tokenService;
        this.properties = properties;
    }

    public void handleLoginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        String token = tokenService.createToken(loginUser);
        String tokenName = properties.getTokenName();
        if (properties.getCookie().isEnable()) {
            writeCookie(response, token, loginUser);
        }
        log.debug("login success: {}", loginUser);
        //响应结果
        Map<String, String> map = Map.of("tokenName", tokenName, "tokenValue", token);
        Result<Map<String, String>> result = Result.successData(map);
        writeResponse(response, result);
    }

    public void handleError(HttpServletRequest request, HttpServletResponse response, Exception e) {
        log.debug("auth failed: error_class: {}, error_msg: {}", e.getClass().getSimpleName(), e.getMessage());
        Result<?> result;
        if (e instanceof AccessDeniedException ae) {
            //登录成功, 权限不足
            result = Result.error(403, ae.getMessage());
        } else if (e instanceof InsufficientAuthenticationException ie) {
            //凭证不足, 访问了需要登录的接口但token校验失败
            result = Result.error("token无效或过期");
        } else if (e instanceof AuthenticationException ae) {
            //其他Authentication异常
            result = Result.error(401, ae.getMessage());
        } else {
            result = Result.error(e.getMessage());
        }
        writeResponse(response, result);
    }

    public void handleLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        //LogoutFilter在TokenAuthenticationFilter之前
        //退出登录, 调用invalidate方法使token失效
        String token = tokenService.getToken(request, properties.getTokenName());
        //使token失效
        if (tokenService.invalidate(token)) {
            //删除cookie
            removeCookie(response);
            writeResponse(response, Result.success("退出登录成功"));
            return;
        }
        writeResponse(response, Result.error(401, "token无效"));
    }

    private void writeCookie(HttpServletResponse response, String token, LoginUser loginUser) {
        WebSecurityProperties.CookieConfig config = properties.getCookie();
        Cookie cookie = new Cookie(properties.getTokenName(), token);
        cookie.setDomain(config.getDomain());
        cookie.setPath(config.getPath());
        cookie.setHttpOnly(config.isHttpOnly());
        cookie.setMaxAge((int) (loginUser.getExpireAt().getTime() - System.currentTimeMillis()) / 1000);
        response.addCookie(cookie);
    }

    private void removeCookie(HttpServletResponse response) {
        WebSecurityProperties.CookieConfig config = properties.getCookie();
        if (config.isEnable()) {
            Cookie cookie = new Cookie(properties.getTokenName(), "");
            cookie.setDomain(config.getDomain());
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
    }

    private <T> void writeResponse(HttpServletResponse response, Result<T> result) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write(result.toJsonString());
        } catch (IOException e) {
            log.error("write response failed: error_msg: {}", e.getMessage());
        }
    }
}
