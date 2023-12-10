package top.wang3.hami.security.ratelimit.annotation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RateMeta {

    /**
     * 最大容量
     */
    private double capacity;

    /**
     * 请求速率 单位秒
     */
    private double rate;

    /**
     * 时间间隔 单位秒 用于固定窗口
     */
    private long interval;

}