package top.wang3.hami.common.dto.notify;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 关注消息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FollowMsg implements Notify {

    private int userId;
    private int followingId;

    @Override
    public int getNotifyType() {
        return NotifyType.FOLLOW.type;
    }
}
