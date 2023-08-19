package top.wang3.hami.security.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.wang3.hami.security.annotation.RateLimit;
import top.wang3.hami.security.model.RateLimiterModel;

import java.util.List;

@Slf4j
@Component
public class RateLimiter implements ApplicationContextAware {

    private List<RateLimiterHandler> handlers;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //没有抛异常
        handlers = applicationContext.getBeansOfType(RateLimiterHandler.class).values().stream().toList();
    }

    /**
     * 是否被限制
     * @param model model
     * @return true-被限制访问
     */
    public boolean limited(RateLimiterModel model) {
        RateLimit.Algorithm algorithm = model.getAlgorithm();
        String key = getKey(model);
        for (RateLimiterHandler handler : handlers) {
            if (handler.support(algorithm)) {
                return !handler.isAllowed(key, model.getRate(), model.getCapacity());
            }
        }
        throw new IllegalArgumentException("No handler found for this algorithm");
    }

    private String getKey(RateLimiterModel model) {
        String prefix = model.getAlgorithm().getPrefix();
        RateLimit.Scope scope = model.getScope();
        String keyName = switch (scope) {
            case IP -> model.getIp();
            case URI -> model.getUri();
            case GLOBAL -> "GLOBAL";
        };
        if (!StringUtils.hasText(keyName)) {
            throw new IllegalStateException("keyName can not be null");
        }
        return prefix + keyName;
    }

}
