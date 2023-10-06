package top.wang3.hami.common.message;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CollectRabbitMessage extends InteractRabbitMessage {

    public CollectRabbitMessage(int userId, int toUserId, byte state, Integer itemId) {
        super(userId, toUserId, state, itemId);
    }

    @Override
    public String getRoute() {
       return getPrefix() + "collect";
    }
}
