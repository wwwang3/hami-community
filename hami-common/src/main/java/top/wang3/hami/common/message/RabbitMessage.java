package top.wang3.hami.common.message;


import top.wang3.hami.common.constant.Constants;

import java.io.Serializable;

public interface RabbitMessage extends Serializable {

    default String getExchange() {
        return Constants.HAMI_TOPIC_EXCHANGE1;
    }

    String getRoute();

}
