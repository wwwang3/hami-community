package top.wang3.hami.security.context;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import top.wang3.hami.security.model.LoginUser;

/**
 * 登录用户上下文, 快速获取LoginUser对象
 */
public class LoginUserContext {

    /**
     * 获取SecurityContext中的LoginUser, 未登录时为空
     * @return LoginUser
     */
    public static LoginUser getLoginUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        return (LoginUser) context.getAuthentication();
    }

    public static int getLoginUserId() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? -1 : loginUser.getId();
    }
}
