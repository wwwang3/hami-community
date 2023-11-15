package top.wang3.hami.common.message;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotifyRabbitReadMessage implements RabbitMessage {

    private Integer receiver;

    private List<Integer> types;

    @Override
    public String getExchange() {
        return RabbitMessage.super.getExchange();
    }

    @Override
    public String getRoute() {
        return "notify.read";
    }
}