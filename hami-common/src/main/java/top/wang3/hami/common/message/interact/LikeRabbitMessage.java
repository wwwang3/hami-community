package top.wang3.hami.common.message.interact;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.dto.interact.LikeType;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class LikeRabbitMessage extends InteractRabbitMessage {

    public LikeRabbitMessage(LikeType likeType) {
        this.likeType = likeType;
    }

    public LikeRabbitMessage(int userId, Integer toUserId, byte state, Integer itemId, LikeType likeType) {
        super(userId, toUserId, state, itemId);
        this.likeType = likeType;
    }

    LikeType likeType;

    @Override
    public String getExchange() {
        return RabbitConstants.HAMI_LIKE_MESSAGE_EXCHANGE;
    }

    @Override
    public String getRoute() {
        //do.like.1.{userId % 5}
        return getPrefix() + "like." + likeType.getType() + "." + getUserId() % 5;
    }


}
