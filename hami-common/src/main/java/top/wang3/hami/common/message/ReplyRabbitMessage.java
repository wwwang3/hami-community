package top.wang3.hami.common.message;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReplyRabbitMessage implements RabbitMessage {

    private Integer userId;
    private Integer articleId;
    private Integer replyId;
    private Integer parentId;
    private String detail;

    @Override
    public String getRoute() {
        return "comment.reply";
    }
}
