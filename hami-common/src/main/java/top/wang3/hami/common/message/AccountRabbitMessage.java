package top.wang3.hami.common.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AccountRabbitMessage implements RabbitMessage {

    private Integer id;
    private EntityMessageType type;


    @Override
    public String getExchange() {
        return "hami-account-exchange";
    }

    @Override
    public String getRoute() {
        return "account" + type.getSuffix();
    }
}
