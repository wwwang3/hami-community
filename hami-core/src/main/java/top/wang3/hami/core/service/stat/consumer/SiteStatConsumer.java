package top.wang3.hami.core.service.stat.consumer;


import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.message.PageRequestLogMessage;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.mapper.SiteStatMapper;

import java.util.List;

@Component
@RabbitListener(
    id = "SiteStatConsumer",
    bindings = {
        @QueueBinding(
            value = @Queue("hami-site-stat-queue-1"),
            exchange = @Exchange(value = RabbitConstants.HAMI_SITE_STAT_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = {"pr.log"}
        ),
    },
    containerFactory = RabbitConstants.BATCH_LISTENER_FACTORY2
)
@RequiredArgsConstructor
@Slf4j
public class SiteStatConsumer {
    private final SiteStatMapper siteStatMapper;

    @RabbitHandler
    @SuppressWarnings("all")
    public void handleMessage(List<PageRequestLogMessage> messages) {
        try {
            int pvIncr = messages.size();
            int uvIncr = 0;
            for (PageRequestLogMessage message : messages) {
                if (StringUtils.hasText(message.ip())) {
                    Long added = RedisClient.getTemplate()
                        .opsForHyperLogLog()
                        .add("site:stat:uv:log", message.ip());
                    if (added != null && added == 1) {
                        uvIncr++;
                    }
                }
            }
            boolean success = ChainWrappers.updateChain(siteStatMapper)
                .setSql(pvIncr != 0, "pv = pv + {0}", pvIncr)
                .setSql(uvIncr != 0, "uv = uv + {0}", uvIncr)
                .eq("id", 1)
                .update();
            log.info("record pv and uv change, pv_incr: {}, uv_incr: {}", pvIncr, uvIncr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
