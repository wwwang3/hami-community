package top.wang3.hami.common.message;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FollowRabbitMessage implements RabbitMessage {

    private Integer userId;
    private Integer following;
    private boolean state;

    @Override
    public String getRoute() {
        return RabbitMessage.getPrefix(state) + "follow"; //do.follow cancel.follow
    }

}
