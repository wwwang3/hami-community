package top.wang3.hami.common.message;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wang3.hami.common.constant.Constants;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchRabbitMessage implements RabbitMessage {

    String keyword;


    @Override
    public String getRoute() {
        return "search.hot";
    }

    @Override
    public String getExchange() {
        return  Constants.HAMI_TOPIC_EXCHANGE2;
    }
}