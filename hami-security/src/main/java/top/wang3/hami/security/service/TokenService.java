package top.wang3.hami.security.service;

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
     * 使token失效
     * @param token 用户登录token
     * @return 是否加入成功
     */
    boolean invalidate(String token);
}
