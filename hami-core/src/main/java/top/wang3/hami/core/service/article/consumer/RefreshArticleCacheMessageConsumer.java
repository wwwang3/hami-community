package top.wang3.hami.core.service.article.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.message.ArticleRabbitMessage;
import top.wang3.hami.common.util.RedisClient;

import java.util.List;


@Component
@Slf4j
@RabbitListener(bindings = {
    @QueueBinding(
        value = @Queue("hami-article-queue-1"),
        exchange = @Exchange(value = RabbitConstants.HAMI_ARTICLE_EXCHANGE, type = "topic"),
        key = {"article.publish", "article.update", "article.delete"}
    ),
}, concurrency = "2")
@RequiredArgsConstructor
public class RefreshArticleCacheMessageConsumer {


    @RabbitHandler
    public void handleArticleMessage(ArticleRabbitMessage message) {
        Integer articleId = message.getArticleId();
        // 文章信息缓存
        String key = RedisConstants.ARTICLE_INFO + articleId;
        // 文章内容缓存
        String contentKey = RedisConstants.ARTICLE_CONTENT + articleId;
        RedisClient.deleteObject(List.of(key, contentKey));
    }
}