package top.wang3.hami.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import top.wang3.hami.common.util.IpUtils;
import top.wang3.hami.security.model.RateLimiterModel;
import top.wang3.hami.security.model.Result;
import top.wang3.hami.security.ratelimit.RateLimiter;

import java.io.IOException;

/**
 * 限流过滤器
 */
@Slf4j
@Setter
public class RateLimiterFilter extends OncePerRequestFilter {

    private String scope;
    private String algorithm;
    private int rate;
    private int capacity;

    private RateLimiter rateLimiter;

    public RateLimiterFilter() {
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        RateLimiterModel model = RateLimiterModel.builder()
                .algorithm(algorithm)
                .scope(scope)
                .rate(rate)
                .capacity(capacity)
                .ip(IpUtils.getIp(request))
                .build();
        if (rateLimiter.limited(model)) {
            //被限制
            log.debug("{}: 操作频繁", request.getRequestURI());
            writeBlockedMessage(response);
            return;
        }
        request.setAttribute("RATE_LIMITED", false);
        filterChain.doFilter(request, response);
        request.removeAttribute("RATE_LIMITED");
    }

    private void writeBlockedMessage(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(Result.error("操作频繁, 请稍后再试").toJsonString());
    }
}
