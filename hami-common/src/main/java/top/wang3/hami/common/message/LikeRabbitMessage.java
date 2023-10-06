package top.wang3.hami.common.message;

import lombok.*;
import top.wang3.hami.common.enums.LikeType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LikeRabbitMessage extends InteractRabbitMessage {

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
