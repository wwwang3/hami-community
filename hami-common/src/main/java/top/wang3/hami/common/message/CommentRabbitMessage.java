package top.wang3.hami.common.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentRabbitMessage implements RabbitMessage {

    private Integer userId; //用户ID
    private Integer articleId; //文章ID
    private Integer commentId; //评论Id
    private String detail;


    @Override
    public String getRoute() {
        return "comment.comment";
    }
}
