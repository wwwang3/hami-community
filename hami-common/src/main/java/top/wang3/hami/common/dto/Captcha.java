package top.wang3.hami.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Captcha {

    /**
     * 类型
     */
    private String type;

    /**
     * 接收验证码的主体
     */
    private String item;

    /**
     * 验证码
     */
    private String value;

    /**
     * 有效期 单位s
     */
    private long expire;
}
