package top.wang3.hami.common.message;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import top.wang3.hami.common.model.Comment;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class ReplyRabbitMessage extends CommentRabbitMessage {

    private Integer parentId;
    private Integer rootId;
    private Integer replyTo;

    public ReplyRabbitMessage(Comment comment, Integer authorId) {
        super(comment, authorId);
        this.replyTo = comment.getReplyTo();
        this.parentId = comment.getParentId();
        this.rootId = comment.getRootId();
    }

    @Override
    public String getRoute() {
        return "comment.reply";
    }
}
