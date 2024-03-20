package top.wang3.hami.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import top.wang3.hami.common.dto.IpInfo;
import top.wang3.hami.common.util.IpUtils;
import top.wang3.hami.security.context.IpContext;

import java.io.IOException;

public class IpContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            IpInfo info = IpUtils.getIpInfo(request);
            IpContext.setIpInfo(info);
            filterChain.doFilter(request, response);
        } finally {
            IpContext.clear();
        }
    }
}
