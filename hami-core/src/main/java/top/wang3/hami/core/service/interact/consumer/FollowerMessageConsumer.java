package top.wang3.hami.core.service.interact.consumer;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.message.interact.FollowRabbitMessage;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.common.util.ZPageHandler;
import top.wang3.hami.core.service.interact.FollowService;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        // 更新被关注用户的粉丝列表
        try {
            if (message == null || message.getToUserId() == null) return;
            String key = RedisConstants.USER_FOLLOWER_LIST + message.getToUserId();
            long timeout = TimeUnit.HOURS.toMillis(24);
            boolean success = RedisClient.pExpire(key, timeout);
            if (success) {
                // 缓存为过期
                List<Number> args = List.of(
                        message.getUserId(),
                        new Date().getTime(),
                        message.getState(),
                        ZPageHandler.DEFAULT_MAX_SIZE
                );
                RedisClient.executeScript(followerScript, List.of(key), args);
            } else {
                // 缓存过期, 直接要用户重新读取
                // 这里不加载缓存，因为关注记录可能还未写入, 让用户重新读取粉丝列表即可
                // ignore it
            }
        } catch (Exception e) {
            // todo 写入失败处理
            log.error("message: {}, error_class: {}, error_msg: {}", message, e.getClass(), e.getMessage());
        }
    }
}
