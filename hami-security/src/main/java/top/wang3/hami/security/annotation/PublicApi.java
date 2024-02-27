package top.wang3.hami.security.annotation;


import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * 开放API
 */
@Documented
@Retention(RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Api(access = AccessControl.PUBLIC)
public @interface PublicApi {

    @AliasFor(annotation = Api.class)
    String httpMethod() default "";

}
