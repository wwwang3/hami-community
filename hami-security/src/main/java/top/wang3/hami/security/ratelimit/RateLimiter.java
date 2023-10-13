package top.wang3.hami.security.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import top.wang3.hami.security.model.RateLimiterModel;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;
import top.wang3.hami.security.ratelimit.handler.RateLimiterHandler;
import top.wang3.hami.security.ratelimit.resolver.RateLimitKeyResolver;

import java.util.List;

@Slf4j
@Component
public class RateLimiter implements ApplicationContextAware {

    private List<RateLimiterHandler> handlers;

    private List<RateLimitKeyResolver> resolvers;

    public static final String RATE_LIMIT_PREFIX = "rate:limit:";

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //没有抛异常
        try {
            handlers = applicationContext.getBeansOfType(RateLimiterHandler.class).values().stream().toList();
            resolvers = applicationContext.getBeansOfType(RateLimitKeyResolver.class).values().stream().toList();
            log.info("load {} rate-limiter-handlers", handlers.size());
            log.info("load {} key-resolvers", resolvers.size());
        } catch (BeansException e) {
            log.error("no handler or resolver found: {}", e);
        }
    }

    /**
     * 是否被限制
     * @param model model
     * @return true-被限制访问
     */
    public boolean limited(RateLimiterModel model) {
        RateLimit.Algorithm algorithm = model.getAlgorithm();
        RateLimit.Scope scope = model.getScope();
        RateLimiterHandler handler = getHandler(algorithm);
        RateLimitKeyResolver resolver = getResolver(scope);
        String key = RATE_LIMIT_PREFIX + algorithm +  ":" + resolver.resolve(model);
        return !handler.isAllowed(key, model.getRate(), model.getCapacity());
    }

    private RateLimiterHandler getHandler(RateLimit.Algorithm algorithm) {
        for (RateLimiterHandler handler : handlers) {
            if (handler.getSupportedAlgorithm().equals(algorithm)) {
                return handler;
            }
        }
        throw new IllegalArgumentException("No handler found for this algorithm");
    }

    private RateLimitKeyResolver getResolver(RateLimit.Scope scope) {
        for (RateLimitKeyResolver resolver: resolvers) {
            if (resolver.getScope().equals(scope)) {
                return resolver;
            }
        }
        throw new IllegalArgumentException("No resolver found for this scope");
    }


}
