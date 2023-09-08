package top.wang3.hami.common.dto.notify;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LikeMsg implements Notify {

    /**
     * 点赞人ID
     */
    int likerId;
    int itemId;
    int itemType;

    @Override
    public int getNotifyType() {
        return NotifyType.LIKE.type;
    }
}
