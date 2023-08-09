package top.wang3.hami.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;
import top.wang3.hami.security.config.WebSecurityProperties;
import top.wang3.hami.security.model.LoginUser;
import top.wang3.hami.security.model.Result;
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
        Map<String, String> map = Map.of(tokenName, token);
        Result<Map<String, String>> result = Result.success(map);
        writeResponse(response, result);
    }

    public void handleError(HttpServletRequest request, HttpServletResponse response, Exception e) {
        log.debug("login failed: error_class: {}, error_msg: {}", e.getClass().getName(), e.getMessage());
        Result<?> result;
        if (e instanceof AccessDeniedException ae) {
            // 登录成功, 校验权限失败
            result = Result.error(403, ae.getMessage());
        } else if (e instanceof AuthenticationException ae) {
            //登录数失败
            result = Result.error(400, ae.getMessage());
        } else {
            result = Result.error(e.getMessage());
        }
        writeResponse(response, result);
    }


    public void handleLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        //退出登录, 调用invalidate方法使token失效
        //从cookie中移除token
        String token = tokenService.getToken(request, properties.getTokenName());
        Assert.notNull(token, "token can not be null");
        if (tokenService.invalidate(token)) {
            //添加黑名单成功
            if (properties.getCookie().isEnable()) {
                Cookie cookie = new Cookie(properties.getTokenName(), "");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
            writeResponse(response, Result.success());
        } else {
            writeResponse(response, Result.error("invalid token"));
        }
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
