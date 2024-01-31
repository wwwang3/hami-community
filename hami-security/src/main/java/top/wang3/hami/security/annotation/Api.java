package top.wang3.hami.security.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Api注解
 * 用在Controller类和Controller方法上，用于配置接口的访问权限
 * 更高级的配置请使用 {@link org.springframework.security.access.prepost.PreAuthorize}
 */
@Documented
@Retention(RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Api {

    String httpMethod() default "";
    AccessControl access() default AccessControl.PUBLIC;
    String[] roles() default {};
    String[] authorities() default {};

}
