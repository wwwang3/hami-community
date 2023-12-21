package top.wang3.hami.core.service.interact.consumer;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.message.interact.FollowRabbitMessage;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.service.interact.FollowService;

@RabbitListener(
        id = "FollowerMessageContainer",
        bindings = @QueueBinding(
                value = @Queue("follower-queue-1"),
                exchange = @Exchange(value = RabbitConstants.HAMI_INTERACT_EXCHANGE, type = ExchangeTypes.TOPIC)
        )
)
@Component
@RequiredArgsConstructor
@Slf4j
public class FollowerMessageConsumer {


    private RedisScript<Long> followerScript;

    private final FollowService followService;

    @PostConstruct
    public void init() {
        followerScript = RedisClient.loadScript("/META-INF/scripts/follow_follower.lua");
    }

    @RabbitHandler
    @SuppressWarnings("all")
    public void handleFollowMessage(FollowRabbitMessage message) {

    }
}
