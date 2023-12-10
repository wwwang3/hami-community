package top.wang3.hami.core.service.article.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.message.ArticleRabbitMessage;
import top.wang3.hami.common.util.RandomUtils;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.service.article.ArticleService;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
@RabbitListener(bindings = {
        @QueueBinding(
                value = @Queue("hami-article-count-queue-1"),
                exchange = @Exchange(value = RabbitConstants.HAMI_ARTICLE_EXCHANGE, type = "topic"),
                key = {"article.publish", "article.delete"}
        ),
}, concurrency = "2")
public class ArticleCountConsumer {

    private final ArticleService articleService;


    @RabbitHandler
    public void handleArticleMessage(ArticleRabbitMessage message) {
        ArticleRabbitMessage.Type type = message.getType();
        if (ArticleRabbitMessage.Type.PUBLISH.equals(type)) {
            updateCount(1);
        } else if (ArticleRabbitMessage.Type.DELETE.equals(type)) {
            updateCount(-1);
        }
    }

    private void updateCount(int delta) {
        String key = RedisConstants.TOTAL_ARTICLE_COUNT;
        if (RedisClient.expire(key, RandomUtils.randomLong(10, 100), TimeUnit.HOURS)) {
            RedisClient.incrBy(key, delta);
        } else {
            articleService.getArticleCount(null);
        }
    }

}
