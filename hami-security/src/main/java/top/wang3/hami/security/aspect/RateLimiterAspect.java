package top.wang3.hami.security.aspect;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.wang3.hami.security.ratelimit.RateLimiter;

@Component
@Slf4j
@Aspect
public class RateLimiterAspect {


    private RateLimiter rateLimiter;

    @Autowired
    public RateLimiterAspect(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Pointcut("@annotation(top.wang3.hami.security.annotation.RateLimit)")
    public void rateLimitAspect() {

    }

    @Before("rateLimitAspect()")
    public void handleLimit(JoinPoint point) {
        //todo
        log.debug("check limit");
        log.debug("{}", point);
    }
}
