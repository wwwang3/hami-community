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
import org.springframework.security.web.util.matcher.RequestMatcherEntry;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import top.wang3.hami.security.context.IpContext;
import top.wang3.hami.security.context.LoginUserContext;
import top.wang3.hami.security.model.Result;
import top.wang3.hami.security.model.WebSecurityProperties;
import top.wang3.hami.security.ratelimit.RateLimitException;
import top.wang3.hami.security.ratelimit.RateLimiter;
import top.wang3.hami.security.ratelimit.annotation.KeyMeta;
import top.wang3.hami.security.ratelimit.annotation.RateLimitModel;
import top.wang3.hami.security.ratelimit.annotation.RateMeta;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * 全局IP限流过滤器
 */
@SuppressWarnings("all")
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    @Setter
    List<RequestMatcherEntry<RateLimitModel>> requestMatcherEntries;

    RateLimiter rateLimiter;

    RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            if (requestMatcherEntries == null || requestMatcherEntries.isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }
            for (RequestMatcherEntry<RateLimitModel> requestMatcherEntry : requestMatcherEntries) {
                if (requestMatcherEntry.getRequestMatcher().matches(request)) {
                    KeyMeta keyMeta = new KeyMeta();
                    buildKeyMeta(keyMeta, request);
                    RateLimitModel rateLimitModel = requestMatcherEntry.getEntry();
                    // Other properties already setting
                    rateLimitModel.setKeyMeta(keyMeta);
                    rateLimiter.checkLimit(rateLimitModel);
                    break;
                }
            }
            // 放行
            filterChain.doFilter(request, response);
        } catch (RateLimitException e) {
            log.error("msg: {}, cause: {}", e.getMessage(), e.getCause() == null ? "null": e.getCause());
            writeBlockedMessage(response, e.getMessage());
        }
    }

    private void buildKeyMeta(KeyMeta keyMeta, HttpServletRequest request) {
        keyMeta.setIp(IpContext.getIpDefaultUnknown());
        keyMeta.setUri(request.getRequestURI());
        HandlerMethod handlerMethod = getHandlerMethod(request);
        if (handlerMethod != null) {
            keyMeta.setMethodName(handlerMethod.getMethod().getName());
            keyMeta.setClassName(handlerMethod.getBeanType().getSimpleName());
        }
        // 需要注意filter顺序, 这个要放在TokenAuthentication之后
        keyMeta.setLoginUserId(LoginUserContext.getOptLoginUserId().map(Objects::toString).orElse(null));
    }

    private HandlerMethod getHandlerMethod(HttpServletRequest request) {
       try {
           HandlerExecutionChain handler = requestMappingHandlerMapping.getHandler(request);
           if (handler != null && handler.getHandler() instanceof HandlerMethod h) {
               return h;
           }
           return null;
       } catch (Exception e) {
           log.warn("get handler-method throw an exception: error_class: {}, error_msg: {}", e.getClass(), e.getMessage());
           return null;
       }
   }

   private void buildRateLimitModel(RateLimitModel model, WebSecurityProperties.ApiRateLimitConfig config) {
       RateMeta rateMeta = config.getRateMeta();
       model.setRateMeta(rateMeta);
       model.setAlgorithm(config.getAlgorithm());
       model.setScope(config.getScope());
       model.setBlockMsg(config.getBlockMsg());
   }

    private void writeBlockedMessage(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(Result.error(message).toJsonString());
    }

    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        requestMappingHandlerMapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        rateLimiter = applicationContext.getBean(RateLimiter.class);
    }

    @Override
    public void afterPropertiesSet() throws ServletException {
        super.afterPropertiesSet();
        if (rateLimiter == null) {
            throw new IllegalArgumentException("rateLimiter cannot be null");
        }
    }
}
