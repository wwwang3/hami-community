package top.wang3.hami.common.dto.notify;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class CommentMsg implements Notify {

    private int userId;
    private int commentId;
    private int commentTo;
    private int articleId;
    private String content;

    @Override
    public int getNotifyType() {
        return NotifyType.COMMENT.type;
    }
}
