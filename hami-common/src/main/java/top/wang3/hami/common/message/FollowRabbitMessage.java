package top.wang3.hami.common.message;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class FollowRabbitMessage extends InteractRabbitMessage {

    public FollowRabbitMessage(int userId, int toUserId, byte state, Integer itemId) {
        super(userId, toUserId, state, itemId);
    }

    @Override
    public String getRoute() {
        return getPrefix() + "follow"; //do.follow cancel.follow
    }

}
