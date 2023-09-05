package top.wang3.hami.common.dto.notify;


import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 评论回复消息
 */
@Data
@AllArgsConstructor
public class ReplyMsg {
    private int userId;
    private int replyTo;
    private int replyId;
    private int articleId;
}
