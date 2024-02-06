package top.wang3.hami.core.service.stat.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.message.ArticleRabbitMessage;
import top.wang3.hami.common.message.user.UserRabbitMessage;
import top.wang3.hami.core.service.stat.repository.ArticleStatRepository;
import top.wang3.hami.core.service.stat.repository.UserStatRepository;

@RabbitListener(
        id = "StatMsgConsumer",
        bindings = {
                @QueueBinding(
                        value = @Queue(RabbitConstants.STAT_QUEUE_5),
                        exchange = @Exchange(value = RabbitConstants.HAMI_ARTICLE_EXCHANGE, type = ExchangeTypes.TOPIC),
                        key = {"article.delete"}
                ),
                @QueueBinding(
                        value = @Queue(RabbitConstants.USER_STAT_QUEUE_6),
                        exchange = @Exchange(value = RabbitConstants.HAMI_USER_EXCHANGE, type = ExchangeTypes.TOPIC),
                        key = {"user.delete"}
                ),
        },
        concurrency = "2"
)
@RequiredArgsConstructor
@Slf4j
public class StatMessageConsumer {

    private final ArticleStatRepository articleStatRepository;
    private final UserStatRepository userStatRepository;

    @RabbitHandler
    public void handleArticleDeleteMessage(ArticleRabbitMessage message) {
        try {
            // 删除文章数据
            articleStatRepository.removeById(message.getArticleId());
        } catch (Exception e) {
            log.error("delete article-stat failed, id: {}, error_class: {}, error_msg: {} ",
                    message.getArticleId(), e.getClass(), e.getMessage());
        }
    }

    @RabbitHandler
    public void handleUserDeleteMessage(UserRabbitMessage message) {
        try {
            // 删除用户数据
            userStatRepository.removeById(message.getUserId());
        } catch (Exception e) {
            log.error("delete article-stat failed, id: {}, error_class: {}, error_msg: {} ",
                    message.getUserId(), e.getClass(), e.getMessage());
        }
    }
}
