package top.wang3.hami.core.service.search.consumer;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.message.SearchRabbitMessage;
import top.wang3.hami.common.util.RedisClient;

import java.util.List;

@Component
@RabbitListener(
        id = "SearchMsgContainer",
        bindings = @QueueBinding(
                value = @Queue(value = "hami-search-queue-1"),
                exchange = @Exchange(value = RabbitConstants.HAMI_SEARCH_EXCHANGE, type = ExchangeTypes.TOPIC),
                key = {"search.hot"}
        )
)
@Slf4j
public class SearchMessageConsumer {

    RedisScript<Long> redisScript;

    @PostConstruct
    public void init() {
        redisScript = RedisClient.loadScript("/META-INF/scripts/hot_search.lua");
    }

    @RabbitHandler
    public void handleMessage(SearchRabbitMessage message) {
        String keyword = message.getKeyword();
        RedisClient.executeScript(
                redisScript,
                List.of(RedisConstants.HOT_SEARCH),
                List.of(keyword)
        );
    }
}
