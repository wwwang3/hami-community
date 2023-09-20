package top.wang3.hami.common.message;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDeletedRabbitMessage implements RabbitMessage {

    private Integer articleId;
    private int deletedCount;

    @Override
    public String getRoute() {
        return "comment.delete";
    }
}
