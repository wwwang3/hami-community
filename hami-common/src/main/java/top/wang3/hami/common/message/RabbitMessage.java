package top.wang3.hami.common.message;


import com.fasterxml.jackson.annotation.JsonIgnore;
import top.wang3.hami.common.constant.RabbitConstants;

import java.io.Serializable;

public interface RabbitMessage extends Serializable {

    @JsonIgnore
    default String getExchange() {
        return RabbitConstants.HAMI_TOPIC_EXCHANGE1;
    }

    @JsonIgnore
    String getRoute();

}
