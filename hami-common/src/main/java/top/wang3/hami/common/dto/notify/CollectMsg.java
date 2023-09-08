package top.wang3.hami.common.dto.notify;


import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 收藏消息
 */
@Data
@AllArgsConstructor
public class CollectMsg implements Notify {

    private int userId;
    private int articleId;

    @Override
    public int getNotifyType() {
        return NotifyType.COLLECT.type;
    }
}
