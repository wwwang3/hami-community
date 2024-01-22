package top.wang3.hami.common.message.interact;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.message.RabbitMessage;
import top.wang3.hami.common.model.Comment;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentRabbitMessage implements RabbitMessage {

    private Integer userId; //用户ID
    private Integer articleId; //文章ID
    private Integer authorId; //文章作者ID
    private Integer commentId; //评论Id
    private String detail;

    public CommentRabbitMessage(Comment comment, Integer authorId) {
        this.userId = comment.getUserId();
        this.articleId = comment.getArticleId();
        this.authorId = authorId;
        this.commentId = comment.getId();
        this.detail = comment.getContent();
    }

    @Override
    public String getExchange() {
        return RabbitConstants.HAMI_COMMENT_EXCHANGE;
    }


    @Override
    public String getRoute() {
        return "comment.comment";
    }
}
