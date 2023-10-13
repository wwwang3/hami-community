package top.wang3.hami.security.ratelimit.aspect;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import top.wang3.hami.security.context.IpContext;
import top.wang3.hami.security.model.RateLimiterModel;
import top.wang3.hami.security.ratelimit.RateLimitException;
import top.wang3.hami.security.ratelimit.RateLimiter;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;

import java.lang.reflect.Method;

@Component
@Aspect
@RequiredArgsConstructor
@Slf4j
public class RateLimiterAspect {


    private final RateLimiter rateLimiter;

    @Pointcut(value = "@annotation(top.wang3.hami.security.ratelimit.annotation.RateLimit)")
    public void rateLimitPointCut() {

    }

    @Before(value = "rateLimitPointCut()")
    public void proceed(JoinPoint point) throws RateLimitException {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        String className = signature.getDeclaringType().getSimpleName();
        RateLimiterModel model = buildModel(method, className);

        if (rateLimiter.limited(model)) {
            throw new RateLimitException("[%s#%s] is limited rate: %s capacity: %s".formatted(className, method.getName(), model.getRate(), model
                    .getCapacity()));
        }
    }

    private RateLimiterModel buildModel(Method method, String className)  {
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        return RateLimiterModel
                .builder()
                .methodName(method.getName())
                .className(className)
                .rate(rateLimit.rate())
                .capacity(rateLimit.capacity())
                .algorithm(rateLimit.algorithm())
                .scope(rateLimit.scope())
                .ip(IpContext.getIpDefaultUnknown())
                .build();
    }
}
