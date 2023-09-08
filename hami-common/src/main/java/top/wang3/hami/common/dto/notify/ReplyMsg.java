package top.wang3.hami.common.dto.notify;


import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 评论回复消息
 */
@Data
@AllArgsConstructor
public class ReplyMsg implements Notify {
    private int userId;
    private int replyTo;
    private int parent;
    private int replyId;
    private int articleId;
    private String content;

    @Override
    public int getNotifyType() {
        return NotifyType.REPLY.type;
    }
}
