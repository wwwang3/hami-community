package top.wang3.hami.common.message;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.constant.RabbitConstants;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRabbitMessage implements RabbitMessage {

    private Type type;
    private Integer userId;

    @Override
    public String getExchange() {
        return RabbitConstants.HAMI_TOPIC_EXCHANGE2;
    }

    @Override
    public String getRoute() {
        return type.route;
    }

    public enum Type {
        USER_CREATE("user.create"),
        USER_UPDATE("user.update"),
        USER_DELETE("user.delete");

        final String route;

        Type(String route) {
            this.route = route;
        }
    }

}
