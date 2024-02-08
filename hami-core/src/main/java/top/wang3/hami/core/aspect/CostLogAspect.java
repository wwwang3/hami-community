package top.wang3.hami.core.aspect;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class CostLogAspect {

    @Pointcut(value = "@annotation(top.wang3.hami.core.annotation.CostLog)")
    public void costLogAspectPoint() {

    }

    @Around(value = "costLogAspectPoint()")
    public Object printLog(ProceedingJoinPoint point) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = point.getSignature().getName();
        String className = point.getSignature().getDeclaringType().getSimpleName();
        try {
            return point.proceed();
        } finally {
            long end = System.currentTimeMillis();
            log.info("[{}#{}] execute cost: {}ms", className, methodName, end - start);
        }
    }
}
