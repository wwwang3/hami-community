package top.wang3.hami.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import top.wang3.hami.security.context.IpContext;
import top.wang3.hami.security.model.Result;
import top.wang3.hami.security.ratelimit.RateLimitException;
import top.wang3.hami.security.ratelimit.RateLimiter;
import top.wang3.hami.security.ratelimit.annotation.KeyMeta;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;
import top.wang3.hami.security.ratelimit.annotation.RateLimiterModel;
import top.wang3.hami.security.ratelimit.annotation.RateMeta;

import java.io.IOException;

/**
 * 全局IP限流过滤器
 */
@Slf4j
@Setter
public class RateLimitFilter extends OncePerRequestFilter {

    private RateLimit.Algorithm algorithm;
    private int rate;
    private int capacity;

    private RateLimiter rateLimiter;

    RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        RateMeta rateMeta = new RateMeta(capacity, rate, capacity / rate);
        KeyMeta keyMeta = new KeyMeta();
        keyMeta.setIp(IpContext.getIpDefaultUnknown());
        HandlerMethod handlerMethod = getHandlerMethod(request);
        if (handlerMethod != null) {
            keyMeta.setMethodName(handlerMethod.getMethod().getName());
            keyMeta.setClassName(handlerMethod.getBeanType().getSimpleName());
        }
        try {
            RateLimiterModel model = new RateLimiterModel(algorithm, RateLimit.Scope.IP, rateMeta, keyMeta);
            rateLimiter.checkLimit(model);
            filterChain.doFilter(request, response);
        } catch (RateLimitException e) {
            log.error("msg: {}, cause: {}", e.getMessage(), e.getCause() == null ? "null": e.getCause());
            writeBlockedMessage(response);
        }
    }

   private HandlerMethod getHandlerMethod(HttpServletRequest request) {
       try {
           HandlerExecutionChain handler = requestMappingHandlerMapping.getHandler(request);
           if (handler != null && handler.getHandler() instanceof HandlerMethod h) {
               return h;
           }
           return null;
       } catch (Exception e) {
           log.debug(e.getMessage());
           return null;
       }
   }

    private void writeBlockedMessage(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(Result.error("操作频繁, 请稍后再试").toJsonString());
    }

    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        requestMappingHandlerMapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
    }


}
