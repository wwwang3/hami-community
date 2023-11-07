package top.wang3.hami.common.message;


import top.wang3.hami.common.constant.RabbitConstants;

import java.io.Serializable;

public interface RabbitMessage extends Serializable {

    default String getExchange() {
        return RabbitConstants.HAMI_TOPIC_EXCHANGE1;
    }

    String getRoute();

}
