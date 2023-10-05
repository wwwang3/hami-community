package top.wang3.hami.core.service.interact.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.message.LikeRabbitMessage;
import top.wang3.hami.core.repository.ArticleRepository;
import top.wang3.hami.core.repository.CommentRepository;
import top.wang3.hami.core.service.interact.LikeService;

@Component
@Slf4j
@RequiredArgsConstructor
//todo 消费失败先不管 _(≧∇≦」∠)_
public class LikerMessageConsumer {

    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final LikeService likeService;


    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue("hami-user-interact-queue-2"),
                    exchange = @Exchange(value = Constants.HAMI_TOPIC_EXCHANGE1, type = "topic"),
                    key = {"*.like.*"}
            ),
    })
    public void handleLikeMessageForList(LikeRabbitMessage message) {

    }

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue("hami-user-interact-queue-3"),
                    exchange = @Exchange(value = Constants.HAMI_TOPIC_EXCHANGE1, type = "topic"),
                    key = {"*.like.*"}
            ),
    })
    public void handleLikeMessageForCount(LikeRabbitMessage message) {

    }
}
