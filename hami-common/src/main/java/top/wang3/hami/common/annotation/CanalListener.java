package top.wang3.hami.common.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CanalListener {

    /**
     * 处理的table名称
     * @return table表名称
     */
    String value() default "";
}
