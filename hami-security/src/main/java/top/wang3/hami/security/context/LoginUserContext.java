package top.wang3.hami.security.context;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import top.wang3.hami.common.model.IpInfo;
import top.wang3.hami.security.model.LoginUser;

/**
 * 登录用户上下文, 快速获取LoginUser对象
 */
public class LoginUserContext {

    private static final ThreadLocal<IpInfo> IP_INFO_THREAD_LOCAL = new ThreadLocal<>();

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

    public static IpInfo getIpInfo() {
        return IP_INFO_THREAD_LOCAL.get();
    }

    public static void setIpInfo(IpInfo info) {
        Assert.notNull(info, "info can not be null");
        IP_INFO_THREAD_LOCAL.set(info);
    }

    public static void clearIpInfo() {
        IP_INFO_THREAD_LOCAL.remove();
    }
}
