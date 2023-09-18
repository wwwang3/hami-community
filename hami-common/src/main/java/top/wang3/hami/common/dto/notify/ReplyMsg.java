package top.wang3.hami.common.dto.notify;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评论回复消息
 */
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ReplyMsg implements Notify {
    private int userId;
    private int replyTo;
    private int replyId;
    private int articleId;
    private String content;

    @Override
    public int getNotifyType() {
        return NotifyType.REPLY.type;
    }
}
