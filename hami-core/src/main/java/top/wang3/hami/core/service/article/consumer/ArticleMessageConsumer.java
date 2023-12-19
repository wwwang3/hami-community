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
import top.wang3.hami.core.service.stat.ArticleStatService;

import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleMessageConsumer {

    private final ArticleStatService articleStatService;

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue("hami-article-queue-1"),
                    exchange = @Exchange(value = RabbitConstants.HAMI_ARTICLE_EXCHANGE, type = "topic"),
                    key = {"article.update", "article.delete"}
            ),
    }, concurrency = "2")
    public void handleArticleMessage(ArticleRabbitMessage message) {
        Integer articleId = message.getArticleId();
        //文章信息缓存
        String key = RedisConstants.ARTICLE_INFO + articleId;
        //文章内容缓存
        String contentKey = RedisConstants.ARTICLE_CONTENT + articleId;
        RedisClient.deleteObject(List.of(key, contentKey));
    }
}
