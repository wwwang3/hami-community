package top.wang3.hami.security.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import top.wang3.hami.security.model.RateLimiterModel;

import java.util.List;

@Slf4j
@Component
public class RateLimiter implements ApplicationContextAware {

    private List<RateLimiterHandler> handlers;

    private List<RateLimitKeyResolver> resolvers;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //没有抛异常
        handlers = applicationContext.getBeansOfType(RateLimiterHandler.class).values().stream().toList();
        resolvers = applicationContext.getBeansOfType(RateLimitKeyResolver.class).values().stream().toList();
    }

    /**
     * 是否被限制
     * @param model model
     * @return true-被限制访问
     */
    public boolean limited(RateLimiterModel model) {
        String algorithm = model.getAlgorithm();
        String scope = model.getScope();
        RateLimiterHandler handler = getHandler(algorithm);
        RateLimitKeyResolver resolver = getResolver(scope);
        String key = "rate:limit:" + algorithm + resolver.resolve(model);
        return handler.isAllowed(key, model.getRate(), model.getCapacity());
    }

    private RateLimiterHandler getHandler(String algorithm) {
        for (RateLimiterHandler handler : handlers) {
            if (handler.getSupportedAlgorithm().equalsIgnoreCase(algorithm)) {
                return handler;
            }
        }
        throw new IllegalArgumentException("No handler found for this algorithm");
    }

    private RateLimitKeyResolver getResolver(String scope) {
        for (RateLimitKeyResolver resolver: resolvers) {
            if (resolver.getScope().equalsIgnoreCase(scope)) {
                return resolver;
            }
        }
        throw new IllegalArgumentException("No resolver found for this scope");
    }


}
