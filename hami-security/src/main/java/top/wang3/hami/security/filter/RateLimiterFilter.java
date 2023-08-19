package top.wang3.hami.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import top.wang3.hami.common.util.IpUtils;
import top.wang3.hami.security.annotation.RateLimit;
import top.wang3.hami.security.model.RateLimiterModel;
import top.wang3.hami.security.model.Result;
import top.wang3.hami.security.ratelimit.RateLimiter;

import java.io.IOException;
import java.util.Optional;

/**
 * 限流过滤器
 */
@Slf4j
public class RateLimiterFilter extends OncePerRequestFilter {

    private RateLimit.Scope scope;
    private RateLimit.Algorithm algorithm;
    private int rate;
    private int capacity;

    private RateLimiter rateLimiter;

    public RateLimiterFilter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String ip = Optional.ofNullable(IpUtils.getIp(request))
                .orElse("unknown");
        RateLimiterModel model = new RateLimiterModel();
        model.setAlgorithm(algorithm);
        model.setScope(scope);
        model.setIp(ip);
        model.setUri(request.getRequestURI());
        model.setRate(rate);
        model.setCapacity(capacity);
        if (rateLimiter.limited(model)) {
            //被限制
            log.debug("{}:操作频繁", request.getRequestURI());
            writeBlockedMessage(response);
        }
        filterChain.doFilter(request, response);
    }

    private void writeBlockedMessage(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(Result.error("操作频繁, 请稍后再试").toJsonString());
    }
}
