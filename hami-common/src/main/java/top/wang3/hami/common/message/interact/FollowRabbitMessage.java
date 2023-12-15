package top.wang3.hami.common.message.interact;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FollowRabbitMessage extends InteractRabbitMessage {

    public FollowRabbitMessage() {
    }

    public FollowRabbitMessage(int userId, int toUserId, byte state, Integer itemId) {
        super(userId, toUserId, state, itemId);
    }

    @Override
    public String getRoute() {
        // do.follow.[1-5] cancel.follow.[1-5]
        return getPrefix() + "follow." + getUserId() % 5;
    }

}
