package top.wang3.hami.security.ratelimit.annotation;


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
     *
     * @return algorithm
     */
    Algorithm algorithm() default Algorithm.SLIDE_WINDOW;

    /**
     * 最大容量
     *
     * @return capacity
     */
    double capacity() default 200;

    /**
     * 请求速率
     */
    double rate() default 20;

    /**
     * 范围, 决定key生成
     *
     * @return scope
     */
    Scope scope() default Scope.IP;


    @Getter
    enum Algorithm {

        /**
         * 固定窗口算法
         * capacity / rate == 时间窗口 比如 200 / 20 ==> 10秒内只能有200个请求
         */
        FIXED_WINDOW("fixed_window"),
        /**
         * 滑动窗口
         */
        SLIDE_WINDOW("slide_window");

        private final String type;

        Algorithm(String type) {
            this.type = type;
        }
    }

    @Getter
    enum Scope {

        /**
         * IP范围限流
         */
        IP("ip"),

        /**
         * 登录用户限流, 未登录直接返回
         */
        LOGIN_USER("login_user"),

        /**
         * 类+方法名称
         */
        METHOD("method"),

        /**
         * 全局
         */
        GLOBAL("global");

        private final String desc;

        Scope(String desc) {
            this.desc = desc;
        }
    }

}
