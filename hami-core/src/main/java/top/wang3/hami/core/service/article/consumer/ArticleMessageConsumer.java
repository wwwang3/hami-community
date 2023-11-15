package top.wang3.hami.core.service.article.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.message.ArticleRabbitMessage;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.service.article.ArticleStatService;

import java.util.List;
import java.util.concurrent.TimeUnit;


@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleMessageConsumer {

    private final ArticleStatService articleStatService;

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue("hami-article-queue-1"),
                    exchange = @Exchange(value = RabbitConstants.HAMI_TOPIC_EXCHANGE2, type = "topic"),
                    key = {"article.update", "article.delete"}
            ),
    }, concurrency = "2")
    public void handleArticleMessage(ArticleRabbitMessage message) {
        //删除缓存
        String key = RedisConstants.ARTICLE_INFO + message.getArticleId();
        String contentKey = RedisConstants.ARTICLE_CONTENT + message.getArticleId();
        RedisClient.deleteObject(List.of(key, contentKey));
    }

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue("hami-article-queue-2"),
                    exchange = @Exchange(value = RabbitConstants.HAMI_TOPIC_EXCHANGE2, type = "topic"),
                    key = {"article.view"}
            ),
    }, concurrency = "2")
    public void handleArticleViewMessage(ArticleRabbitMessage message) {
        String ip = message.getIp();
        if (ip == null) return;
        String redisKey = RedisConstants.VIEW_LIMIT + ip + ":" + message.getArticleId();
        boolean success = RedisClient.setNx(redisKey, "view-lock", 15, TimeUnit.SECONDS);
        if (!success) {
            log.debug("ip: {} access repeat", ip);
            return;
        }
        //增加阅读量
        articleStatService.increaseViews(message.getArticleId(), 1);
    }
}
