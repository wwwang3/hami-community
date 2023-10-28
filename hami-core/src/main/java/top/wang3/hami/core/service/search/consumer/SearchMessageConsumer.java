package top.wang3.hami.core.service.search.consumer;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.message.SearchRabbitMessage;
import top.wang3.hami.common.util.RedisClient;

import java.util.List;

@Component
@RabbitListener(
        bindings = @QueueBinding(
                value = @Queue(value = "hami-search-interact-queue-1"),
                exchange = @Exchange(value = Constants.HAMI_TOPIC_EXCHANGE2, type = "topic"),
                key = {"search.hot"}
        ),
        concurrency = "4"
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
        log.debug("handle search message");
        String keyword = message.getKeyword();
        Long result = RedisClient.executeScript(redisScript,
                List.of(Constants.HOT_SEARCH),
                List.of(keyword));
//        Double result = RedisClient.zIncr(Constants.HOT_SEARCH, keyword, 1);
        log.debug("result: {}", result);
    }
}
