package top.wang3.hami.common.message.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.dto.IpInfo;
import top.wang3.hami.common.message.RabbitMessage;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRabbitMessage implements RabbitMessage {

    private int id;
    private IpInfo ipInfo;
    private long loginTime;

    @Override
    @JsonIgnore
    public String getExchange() {
        return RabbitConstants.HAMI_ACCOUNT_EXCHANGE;
    }

    @Override
    @JsonIgnore
    public String getRoute() {
        return "login.success";
    }
}
