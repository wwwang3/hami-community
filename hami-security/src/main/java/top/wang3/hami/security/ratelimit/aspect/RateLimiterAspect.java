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
import top.wang3.hami.security.context.LoginUserContext;
import top.wang3.hami.security.ratelimit.RateLimitException;
import top.wang3.hami.security.ratelimit.RateLimiter;
import top.wang3.hami.security.ratelimit.annotation.KeyMeta;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;
import top.wang3.hami.security.ratelimit.annotation.RateLimiterModel;
import top.wang3.hami.security.ratelimit.annotation.RateMeta;

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
        rateLimiter.checkLimit(model);
    }

    private RateLimiterModel buildModel(Method method, String className)  {
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        RateMeta rateMeta = new RateMeta(rateLimit.capacity(), rateLimit.rate(), rateLimit.interval());

        KeyMeta keyMeta = new KeyMeta();
        keyMeta.setIp(IpContext.getIpDefaultUnknown());
        keyMeta.setClassName(className);
        keyMeta.setMethodName(method.getName());
        Integer loginUserId = LoginUserContext.getLoginUserIdDefaultNull();
        keyMeta.setLoginUserId(loginUserId == null ? null : loginUserId.toString());

        RateLimiterModel model = new RateLimiterModel();
        model.setAlgorithm(rateLimit.algorithm());
        model.setScope(rateLimit.scope());
        model.setRateMeta(rateMeta);
        model.setKeyMeta(keyMeta);
        model.setBlockMsg(rateLimit.blockMsg());
        return model;
    }
}
