package top.wang3.hami.security;

import org.springframework.context.annotation.Import;
import top.wang3.hami.security.config.TokenServiceConfig;
import top.wang3.hami.security.config.WebSecurityConfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(value = {WebSecurityConfig.class, TokenServiceConfig.class})
public @interface EnableSecurity {
}
