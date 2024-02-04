package top.wang3.hami.security.context;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.wang3.hami.common.dto.IpInfo;
import top.wang3.hami.security.exception.NotLoginException;
import top.wang3.hami.security.model.LoginUser;

import java.util.Optional;

/**
 * 登录用户上下文, 快速获取LoginUser对象
 */
public class LoginUserContext {

    /**
     * 获取SecurityContext中的LoginUser, 未登录时为空
     * @return LoginUser
     * @throws NotLoginException 未登录时时抛出
     */
    @NonNull
    public static LoginUser getLoginUser() throws NotLoginException {
        return getOptLoginUser()
                .orElseThrow(NotLoginException::new);
    }

    public static Optional<LoginUser> getOptLoginUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        if (authentication == null) return Optional.empty();
        // fix: 匿名登录时, 获取的principal不是LoginUser而是anonymousUser
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUser user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public static int getLoginUserId() throws NotLoginException {
        return getOptLoginUser()
                .map(LoginUser::getId)
                .orElseThrow(NotLoginException::new);
    }

    public static Integer getLoginUserIdDefaultNull() {
        return getOptLoginUser()
                .map(LoginUser::getId)
                .orElse(null);
    }

    public static Optional<Integer> getOptLoginUserId() {
        return getOptLoginUser()
                .map(LoginUser::getId);
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
