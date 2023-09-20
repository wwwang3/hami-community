package top.wang3.hami.common.message;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectRabbitMessage implements RabbitMessage {


    private int userId;
    private int articleId;
    private boolean state;

    @Override
    public String getRoute() {
       return RabbitMessage.getPrefix(state) + "collect";
    }
}
