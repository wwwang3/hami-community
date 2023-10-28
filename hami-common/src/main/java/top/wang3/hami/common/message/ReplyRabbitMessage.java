package top.wang3.hami.common.message;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wang3.hami.common.model.Comment;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReplyRabbitMessage implements RabbitMessage {

    private Integer userId; //用户ID
    private Integer articleId; //文章ID
    private Integer authorId; //文章作者ID
    private Integer commentId; //评论Id
    private String detail;
    private Integer parentId;
    private Integer rootId;
    private Integer replyTo;

    public ReplyRabbitMessage(Comment comment, Integer authorId) {
        this.userId = comment.getUserId();
        this.articleId = comment.getArticleId();
        this.authorId = authorId;
        this.commentId = comment.getId();
        this.detail = comment.getContent();
        this.replyTo = comment.getReplyTo();
        this.parentId = comment.getParentId();
        this.rootId = comment.getRootId();
    }

    public String getRoute() {
        return "comment.reply";
    }
}
