package top.wang3.hami.common.message.email;

import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.message.RabbitMessage;


public interface EmailRabbitMessage extends RabbitMessage, MailMessage {

    @Override
    default String getExchange() {
        return RabbitConstants.HAMI_EMAIL_EXCHANGE;
    }

    @Override
    default String getRoute() {
        return RabbitConstants.EMAIL_ROUTING;
    }
}
