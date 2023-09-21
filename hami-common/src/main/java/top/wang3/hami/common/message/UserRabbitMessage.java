package top.wang3.hami.common.message;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wang3.hami.common.constant.Constants;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRabbitMessage implements RabbitMessage {


    private int type;
    private Integer userId;

    @Override
    public String getExchange() {
        return Constants.HAMI_TOPIC_EXCHANGE2;
    }

    @Override
    public String getRoute() {
        if (type == 1) {
            return "user.insert";
        } else if (type == 2) {
            return "user.update";
        } else {
            return "user.delete";
        }
    }
}
