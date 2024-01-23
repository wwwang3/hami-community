package top.wang3.hami.core.service.comment.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.message.interact.InteractRabbitMessage;
import top.wang3.hami.common.message.interact.LikeRabbitMessage;
import top.wang3.hami.common.model.Comment;
import top.wang3.hami.core.service.comment.repository.CommentRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RabbitListeners(value = {
        @RabbitListener(
                id = "CommentMessageContainer-2",
                bindings = @QueueBinding(
                        value = @Queue(RabbitConstants.COMMENT_QUEUE_1),
                        exchange = @Exchange(value = RabbitConstants.HAMI_INTERACT_EXCHANGE, type = ExchangeTypes.TOPIC),
                        key = "*.like.2.*"
                ),
                containerFactory = "batchRabbitListenerContainerFactory"
        ),
})
@Component
@RequiredArgsConstructor
@Slf4j
public class CommentLikesConsumer {

    private final CommentRepository commentRepository;


    @RabbitHandler
    public void handleLikeMessage(List<LikeRabbitMessage> messages) {
        try {
            List<Comment> comments = messages.stream()
                    .filter(m -> m.getToUserId() != 0)
                    .collect(Collectors.groupingBy(InteractRabbitMessage::getItemId))
                    .values()
                    .stream()
                    .map(msgs -> {
                        LikeRabbitMessage message = msgs.get(0);
                        Comment comment = new Comment();
                        comment.setId(message.getItemId());
                        comment.setLikes(0);
                        return msgs.stream().reduce(comment, (item, msg) -> {
                            item.setLikes(item.getLikes() + delta(msg.getState()));
                            return item;
                        }, (v1, v2) -> v1);
                    })
                    .filter(s -> !Objects.equals(0, s.getLikes()))
                    .toList();
            commentRepository.batchUpdateLikes(comments);
        } catch (Exception e) {
            log.error("error_class: {}, error_msg: {}", e.getClass(), e.getMessage());
        }
    }

    private int delta(byte state) {
        return state == 1 ? 1 : -1;
    }
}
