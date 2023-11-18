package top.wang3.hami.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import top.wang3.hami.common.component.SnowflakeIdGenerator;
import top.wang3.hami.common.util.IpUtils;
import top.wang3.hami.security.model.RateLimiterModel;
import top.wang3.hami.security.model.Result;
import top.wang3.hami.security.ratelimit.RateLimitException;
import top.wang3.hami.security.ratelimit.RateLimiter;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;

import java.io.IOException;

/**
 * 全局IP限流过滤器
 */
@Slf4j
@Setter
public class RateLimitFilter extends OncePerRequestFilter {

    private RateLimit.Scope scope;
    private RateLimit.Algorithm algorithm;
    private int rate;
    private int capacity;

    private RateLimiter rateLimiter;

    RequestMappingHandlerMapping requestMappingHandlerMapping;
    SnowflakeIdGenerator generator;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long reqId = generator.nextId();
        MDC.put("reqId", String.valueOf(reqId));
        RateLimiterModel model = RateLimiterModel.builder()
                .algorithm(algorithm)
                .scope(scope)
                .rate(rate)
                .uri(request.getRequestURI())
                .capacity(capacity)
                .ip(IpUtils.getIp(request))
                .build();
        HandlerMethod handlerMethod = getHandlerMethod(request);
        if (handlerMethod != null) {
            model.setMethodName(handlerMethod.getMethod().getName());
            model.setClassName(handlerMethod.getBeanType().getName());
        }
        try {
            rateLimiter.checkLimit(model);
        } catch (RateLimitException e) {
            log.error("e: {}", e.getMessage());
            writeBlockedMessage(response);
            return;
        }
        filterChain.doFilter(request, response);
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
