package top.wang3.hami.security.annotation;


import lombok.Getter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于AOP
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 限流算法
     * @return algorithm
     */
    Algorithm algorithm() default Algorithm.SLIDE_WINDOW;

    /**
     * 最大容量
     * @return capacity
     */
    int capacity() default 200;

    /**
     * 请求速率
     */
    int rate() default 20;

    /**
     * 范围, 决定key生成
     * @return scope
     */
    Scope scope() default Scope.URI;


    @Getter
    enum Algorithm {
        /*
        固定窗口
         */
        FIXED_WINDOW("rate:limit:fixed_window"),
        /**
         * 滑动窗口
         */
        SLIDE_WINDOW("rate:limit:slide_window");

        private final String prefix;

        Algorithm(String prefix) {
            this.prefix = prefix;
        }

    }

    enum Scope {
        GLOBAL, //全局
        IP, //IP
        URI; //请求接口;
    }
}
