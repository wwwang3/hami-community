package top.wang3.hami.common.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wang3.hami.common.constant.RabbitConstants;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AccountRabbitMessage implements RabbitMessage {

    private Integer id;
    private EntityMessageType type;


    @Override
    public String getExchange() {
        return RabbitConstants.HAMI_ACCOUNT_EXCHANGE;
    }

    @Override
    public String getRoute() {
        return "account" + type.getSuffix();
    }
}
