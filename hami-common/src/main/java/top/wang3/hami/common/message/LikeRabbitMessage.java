package top.wang3.hami.common.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import top.wang3.hami.common.enums.LikeType;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class LikeRabbitMessage extends InteractRabbitMessage {

    public LikeRabbitMessage(LikeType likeType) {
        this.likeType = likeType;
    }

    public LikeRabbitMessage(int userId, int toUserId, byte state, Integer itemId, LikeType likeType) {
        super(userId, toUserId, state, itemId);
        this.likeType = likeType;
    }

    LikeType likeType;

    @Override
    public String getRoute() {
        return getPrefix() + "like." + likeType.getType(); //do.like.1 //do.like.2
    }


}
