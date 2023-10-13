package top.wang3.hami.core.service.article.consumer;


import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.message.ArticleRabbitMessage;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.service.article.ArticleStatService;

import java.util.List;


@Component
@RequiredArgsConstructor
public class ArticleMessageConsumer {

    private final ArticleStatService articleStatService;

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue("hami-article-queue-1"),
                    exchange = @Exchange(value = Constants.HAMI_TOPIC_EXCHANGE2, type = "topic"),
                    key = {"article.*"}
            ),
    }, concurrency = "2")
    public void handleArticleMessage(ArticleRabbitMessage message) {
        if (message.getType() == ArticleRabbitMessage.Type.UPDATE ||
                message.getType() == ArticleRabbitMessage.Type.DELETE) {
            //删除缓存
            String key = Constants.ARTICLE_INFO + message.getArticleId();
            String contentKey = Constants.ARTICLE_CONTENT + message.getArticleId();
            RedisClient.deleteObject(List.of(key, contentKey));
        } else if (message.getType() == ArticleRabbitMessage.Type.VIEW) {
            //文章阅读量增加
            articleStatService.increaseViews(message.getArticleId(), 1);
        }
    }
}
