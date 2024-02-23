package top.wang3.hami.security.ratelimit.annotation;


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
     * 请求速率 请求数/s
     *
     * @return rate
     */
    double rate() default 20;

    /**
     * 最大容量
     *
     * @return capacity
     */
    double capacity() default 200;

    /**
     * 时间间隔 用于固定窗口算法 单位s
     * @return interval
     */
    long interval() default 10;

    /**
     * 范围, 决定key生成
     *
     * @return scope
     */
    Scope scope() default Scope.IP;

    /**
     * 错误消息
     *
     * @return 抛出异常的错误消息
     */
    String blockMsg() default "大哥别刷了( ´･･)ﾉ(._.`)";

    enum Algorithm {

        /**
         * 固定窗口算法
         * capacity / rate == 时间窗口 比如 200 / 20 ==> 10秒内只能有200个请求
         */
        FIXED_WINDOW,

        /**
         * 滑动窗口
         */
        SLIDE_WINDOW

    }

    enum Scope {

        /**
         * IP范围限流 key: ip
         */
        IP,

        IP_URI,

        /**
         * 登录用户限流, 未登录直接返回, key: methodName + loginUserId
         */
        LOGIN_USER,

        /**
         * key: 类+方法名称
         */
        METHOD,

        /**
         * 请求URI key: uri or global when uri is null
         */
        URI,

        /**
         * 全局 key: GLOBAL
         */
        GLOBAL

    }

}
