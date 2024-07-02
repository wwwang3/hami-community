package top.wang3.hami.common.message;

import top.wang3.hami.common.constant.RabbitConstants;

public record PageRequestLogMessage(String ip) implements RabbitMessage {

    @Override
    public String getExchange() {
        return RabbitConstants.HAMI_SITE_STAT_EXCHANGE;
    }

    @Override
    public String getRoute() {
        return "pr.log";
    }
}
