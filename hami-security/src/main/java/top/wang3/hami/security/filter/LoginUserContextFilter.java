package top.wang3.hami.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import top.wang3.hami.common.dto.IpInfo;
import top.wang3.hami.common.util.IpUtils;
import top.wang3.hami.security.context.LoginUserContext;

import java.io.IOException;

public class LoginUserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            IpInfo info = IpUtils.getIpInfo(request);
            LoginUserContext.setIpInfo(info);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            //ignore it
        } finally {
            LoginUserContext.clearIpInfo();
        }
    }
}
