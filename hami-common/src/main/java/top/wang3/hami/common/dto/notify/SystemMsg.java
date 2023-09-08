package top.wang3.hami.common.dto.notify;


import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 系统消息
 */
@Data
@AllArgsConstructor
public class SystemMsg implements Notify {

    private int userId;
    private String content;

    @Override
    public int getNotifyType() {
        return NotifyType.SYSTEM.type;
    }
}
