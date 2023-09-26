package top.wang3.hami.common.message;


import top.wang3.hami.common.constant.Constants;

public interface RabbitMessage {

    default String getExchange() {
        return Constants.HAMI_TOPIC_EXCHANGE1;
    }

    String getRoute();

    static String getPrefix(boolean state) {
        return state ? "do." : "cancel.";
    }

}
