package top.wang3.hami.security.context;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.wang3.hami.common.dto.IpInfo;
import top.wang3.hami.security.exception.NotLoginException;
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
        return (LoginUser) context.getAuthentication().getPrincipal();
    }

    public static int getLoginUserId() throws NotLoginException {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) throw new NotLoginException("未登录");
        return loginUser.getId();
    }

    public static Integer getLoginUserIdDefaultNull() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getId();
    }

    public static IpInfo getIpInfo() {
        return IpContext.getIpInfo();
    }

    public static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes attr) {
            return attr.getRequest();
        }
        return null;
    }

}
