package top.wang3.hami.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.message.RabbitMessage;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Captcha implements RabbitMessage {

    /**
     * 类型 作为redis-key前缀
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

    @Override
    public String getExchange() {
        return Constants.HAMI_DIRECT_EXCHANGE1;
    }

    @Override
    public String getRoute() {
        return Constants.EMAIL_ROUTING;
    }
}
