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
@RequiredArgsConstructor
@Slf4j
@RabbitListener(bindings = {
        @QueueBinding(
                value = @Queue("hami-article-count-queue-2"),
                exchange = @Exchange(value = RabbitConstants.HAMI_ARTICLE_EXCHANGE, type = "topic"),
                key = {"article.publish", "article.delete"}
        ),
}, concurrency = "2")
public class UserArticleCountConsumer {

    private final ArticleService articleService;

    @RabbitHandler
    public void handleArticleMessage(ArticleRabbitMessage message) {
        ArticleRabbitMessage.Type type = message.getType();
        Integer authorId = message.getAuthorId();
        if (ArticleRabbitMessage.Type.PUBLISH.equals(type)) {
            updateCount(authorId, 1);
        } else if (ArticleRabbitMessage.Type.DELETE.equals(type)) {
            updateCount(authorId, -1);
        }
    }

    private void updateCount(Integer userId, int delta) {
        String key = RedisConstants.USER_ARTICLE_COUNT + userId;
        if (RedisClient.expire(key, RandomUtils.randomLong(10, 100), TimeUnit.HOURS)) {
            RedisClient.incrBy(key, delta);
        } else {
            articleService.getUserArticleCount(userId);
        }
    }
}
