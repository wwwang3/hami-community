package top.wang3.hami.common.dto.notify;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommentMsg {

    private int userId;
    private int commentId;
    private int commentTo;
    private int articleId;

}
