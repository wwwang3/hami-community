package top.wang3.hami.common.message;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LikeRabbitMessage implements RabbitMessage {

    Integer userId;
    Integer itemId;
    byte type;
    boolean state;

    @Override
    public String getRoute() {
        return getPrefix() + "like." + type; //do.like.1 //do.like.2
    }

    String getPrefix() {
        return state ? "do." : "cancel.";
    }

}
