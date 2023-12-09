package top.wang3.hami.common.message.interact;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CollectRabbitMessage extends InteractRabbitMessage {


    public CollectRabbitMessage() {
    }

    public CollectRabbitMessage(int userId, Integer toUserId, byte state, Integer itemId) {
        super(userId, toUserId, state, itemId);
    }

    @Override
    public String getRoute() {
        return getPrefix() + "collect";
    }

}
