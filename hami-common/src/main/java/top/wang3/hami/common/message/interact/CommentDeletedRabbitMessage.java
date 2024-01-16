package top.wang3.hami.common.message.interact;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.message.RabbitMessage;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDeletedRabbitMessage implements RabbitMessage {

    private Integer articleId; // 文章Id
    private int deletedCount; // 删除数量
    private int authorId; // 作者ID

    @Override
    public String getExchange() {
        return RabbitConstants.HAMI_INTERACT_EXCHANGE;
    }

    @Override
    public String getRoute() {
        return "comment.delete";
    }
}
