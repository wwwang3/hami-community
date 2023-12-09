package top.wang3.hami.security.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.wang3.hami.security.ratelimit.algorithm.RateLimiterAlgorithm;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;
import top.wang3.hami.security.ratelimit.annotation.RateLimiterModel;
import top.wang3.hami.security.ratelimit.annotation.RateMeta;
import top.wang3.hami.security.ratelimit.resolver.RateLimitKeyResolver;

import java.util.List;

@Slf4j
@Component
public class RateLimiter implements ApplicationContextAware {

    private List<RateLimiterAlgorithm> algorithms;

    private List<RateLimitKeyResolver> resolvers;

    public static final String RATE_LIMIT_PREFIX = "rate:limit:";

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //没有抛异常
        try {
            algorithms = applicationContext.getBeansOfType(RateLimiterAlgorithm.class).values().stream().toList();
            resolvers = applicationContext.getBeansOfType(RateLimitKeyResolver.class).values().stream().toList();
            log.info("find {} rate-limiter-algorithms",algorithms.size());
            log.info("load {} key-resolvers", resolvers.size());
        } catch (BeansException e) {
            log.error("no algorithm or resolver found: {}", e.getMessage());
        }
    }


    /**
     * 检查是否限流
     * @param model model
     * @throws RateLimitException 限流时抛出此异常
     */
    public void checkLimit(RateLimiterModel model) throws RateLimitException {
        try {
            RateLimit.Algorithm algorithm = model.getAlgorithm();
            RateLimit.Scope scope = model.getScope();
            RateLimiterAlgorithm rateLimiterAlgorithm = getAlgorithm(algorithm);
            RateLimitKeyResolver resolver = getResolver(scope);

            RateMeta rateMeta = model.getRateMeta();
            String key = RATE_LIMIT_PREFIX + algorithm + ":" + resolver.resolve(model.getKeyMeta());
            List<Long> results = rateLimiterAlgorithm.execute(key, rateMeta);

            boolean allowed = results.get(0) == 1L;
            Long remain = results.get(1);
            if (!allowed) {
                String msg = "[%s] is limited, rate: %s, capacity: %s".formatted(key, rateMeta.getRate(), rateMeta.getCapacity());
                String blockMsg = model.getBlockMsg();
                throw new RateLimitException(StringUtils.hasText(blockMsg) ? blockMsg : msg);
            }
            log.info("[{}] remain_requests: {}", key, remain);
        } catch (RateLimitException e) {
            throw e;
        } catch (Exception e) {
            throw new RateLimitException(e.getMessage(), e.getCause());
        }
    }


    private RateLimiterAlgorithm getAlgorithm(RateLimit.Algorithm algorithm) {
        for (RateLimiterAlgorithm val : algorithms) {
            if (algorithm.equals(val.getName())) {
                return val;
            }
        }
        throw new RateLimitException("No implementation found for this algorithm.");
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
