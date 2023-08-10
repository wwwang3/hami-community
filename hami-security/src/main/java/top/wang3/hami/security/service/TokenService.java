package top.wang3.hami.security.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.Assert;
import top.wang3.hami.security.model.LoginUser;

public interface TokenService {


    /**
     * 生成token
     * @param loginUser LoginUser
     * @return token
     */
    String createToken(LoginUser loginUser);

    /**
     * 根据登录token解析LoginUser
     * @param token token字符串
     * @return LoginUser-解析成功 null-解析失败
     */
    LoginUser resolveToken(String token);

    /**
     * 从header或者cookie中获取token的默认实现
     * @param request HttpServletRequest
     * @param tokenName token名称
     * @return token, 没有则返回kon
     */
    default String getToken(HttpServletRequest request, String tokenName) {
        Assert.notNull(tokenName, "tokenName can not be null");
        //从请求中获取
        String token = request.getHeader(tokenName);
        if (token != null) return token;
        //从cookie中获取
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (tokenName.equalsIgnoreCase(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * 使token失效, 退出登录时使用
     * @param token 用户登录token
     * @return 是否加入成功 token为空或者解析失败返回false
     */
    boolean invalidate(String token);
}
