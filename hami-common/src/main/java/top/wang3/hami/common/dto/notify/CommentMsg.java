package top.wang3.hami.common.dto.notify;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommentMsg implements Notify {

    private int userId;
    private int commentId;
    private int commentTo;
    private int articleId;
    //评论内容一般不会修改
    private String content;

    @Override
    public int getNotifyType() {
        return NotifyType.COMMENT.type;
    }
}
