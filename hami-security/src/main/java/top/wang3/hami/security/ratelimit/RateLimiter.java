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
            log.error("no handler or resolver found: {}", e.getMessage());
        }
    }


    public void checkLimit(RateLimiterModel model) throws RateLimitException {
        try {
            RateLimit.Algorithm algorithm = model.getAlgorithm();
            RateLimit.Scope scope = model.getScope();
            RateLimiterHandler handler = getHandler(algorithm);
            RateLimitKeyResolver resolver = getResolver(scope);
            String key = RATE_LIMIT_PREFIX + algorithm + ":" + resolver.resolve(model);

            List<Long> results = handler.execute(key, model.getRate(), model.getCapacity());
            boolean allowed = results.get(0) == 1L;
            Long remain = results.get(1);
            if (!allowed) {
                String msg = "[%s] is limited rate: %s, capacity: %s".formatted(key, model.getRate(), model.getScope());
                throw new RateLimitException(msg);
            }
            log.info("[{}] remain_requests: {}", key, remain);
        } catch (RateLimitException e) {
            throw e;
        } catch (Exception e) {
            throw new RateLimitException(e.getMessage(), e.getCause());
        }
    }


    private RateLimiterHandler getHandler(RateLimit.Algorithm algorithm) throws RateLimitException {
        for (RateLimiterHandler handler : handlers) {
            if (handler.getSupportedAlgorithm().equals(algorithm)) {
                return handler;
            }
        }
        throw new RateLimitException("No handler found for this algorithm");
    }

    private RateLimitKeyResolver getResolver(RateLimit.Scope scope) throws RateLimitException {
        for (RateLimitKeyResolver resolver : resolvers) {
            if (resolver.getScope().equals(scope)) {
                return resolver;
            }
        }
        throw new RateLimitException("No resolver found for this scope");
    }


}
