package top.wang3.hami.security.ratelimit.annotation;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RateMeta {

    /**
     * 请求速率 单位秒
     */
    private double rate;

    /**
     * 最大容量
     */
    private double capacity;

    /**
     * 时间间隔 单位秒 用于固定窗口
     */
    private long interval;

    public RateMeta(double rate, double capacity, long interval) {
        this.rate = rate;
        this.capacity = capacity;
        this.interval = interval;
    }
}
