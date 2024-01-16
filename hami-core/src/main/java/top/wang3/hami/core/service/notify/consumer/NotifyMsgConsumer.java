package top.wang3.hami.core.service.notify.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.dto.builder.NotifyMsgBuilder;
import top.wang3.hami.common.dto.interact.LikeType;
import top.wang3.hami.common.message.NotifyRabbitReadMessage;
import top.wang3.hami.common.message.UserRabbitMessage;
import top.wang3.hami.common.message.interact.*;
import top.wang3.hami.common.model.Comment;
import top.wang3.hami.common.model.NotifyMsg;
import top.wang3.hami.core.component.InteractConsumer;
import top.wang3.hami.core.service.comment.repository.CommentRepository;
import top.wang3.hami.core.service.notify.repository.NotifyMsgRepository;
import top.wang3.hami.security.model.Result;

import java.util.List;
import java.util.Objects;

@RabbitListener(bindings = {
        @QueueBinding(
                value = @Queue("hami-notify-queue-1"),
                exchange = @Exchange(value = RabbitConstants.HAMI_INTERACT_EXCHANGE, type = "topic"),
                key = {"do.follow", "do.like.*.*", "do.collect", "comment.*", "notify.read"}
        ),
        @QueueBinding(
                value = @Queue("hami-notify-queue-2"),
                exchange = @Exchange(value = RabbitConstants.HAMI_USER_EXCHANGE, type = "topic"),
                key = {"user.create"}
        )
}, concurrency = "4")
@Component
@RequiredArgsConstructor
@Slf4j
public class NotifyMsgConsumer implements InteractConsumer {

    private final CommentRepository commentRepository;
    private final NotifyMsgRepository notifyMsgRepository;
    public static final String welcome = "欢迎注册使用Hami";


    //点赞 评论 收藏 关注等通知消息
    @Override
    public void handleLikeMessage(LikeRabbitMessage message) {
        //点赞消息
        try {
            //点赞一般需要判断取消后再次点赞的, 数据库加了索引不管了
            if (Constants.ZERO.equals(message.getState())
                    || isSelf(message.getUserId(), message.getToUserId())) {
                return;
            }
            if (LikeType.ARTICLE.equals(message.getLikeType())) {
                //文章点赞
                handleArticleLike(message);
            } else {
                handleCommentLike(message);
            }
        } catch (Exception e) {
            logError(e);
        }
    }

    private void handleArticleLike(LikeRabbitMessage message) {
        //点赞通知 xx赞了你的文章
        int itemUser = message.getToUserId();
        Integer sender = message.getUserId();
        NotifyMsg msg = NotifyMsgBuilder
                .buildArticleLikeMsg(sender, itemUser, message.getItemId());
        notifyMsgRepository.save(msg);
    }

    private void handleCommentLike(LikeRabbitMessage message) {
        //评论点赞 xx赞了你的评论
        Integer commentId = message.getItemId();
        int itemUser = message.getToUserId();
        Comment comment = commentRepository.getById(commentId);
        NotifyMsg msg = NotifyMsgBuilder.buildCommentLikerMsg(message.getUserId(),
                itemUser, commentId, comment.getArticleId(), comment.getContent());
        notifyMsgRepository.save(msg);
    }

    @Override
    public void handleCommentMessage(CommentRabbitMessage message) {
        try {
            //评论文章通知 xx评论了你的文章
            if (isSelf(message.getAuthorId(), message.getUserId())) {
                return;
            }
            String[] details = new String[]{message.getDetail()};
            String detail = Result.writeValueAsString(details);
            NotifyMsg msg = NotifyMsgBuilder.buildCommentMsg(message.getUserId(), message.getAuthorId(),
                    message.getCommentId(), message.getArticleId(), detail);
            save(msg);
        } catch (Exception e) {
            logError(e);
        }
    }

    @Override
    public void handleReplyMessage(ReplyRabbitMessage message) {
        try {
            // 把他爹也查出来 内容一起写入通知 xx回复了你的评论
            // 自己回复自己不需要通知
            Comment comment = commentRepository.getById(message.getParentId());
            if (comment == null || isSelf(message.getUserId(), comment.getUserId())) {
                return;
            }
            String[] details = new String[]{comment.getContent(), message.getDetail()};
            String detail = Result.writeValueAsString(details);
            NotifyMsg msg = NotifyMsgBuilder
                    .buildReplyMsg(
                            message.getUserId(), message.getReplyTo(),
                            message.getCommentId(), message.getArticleId(),
                            detail
                    );
            save(msg);
        } catch (Exception e) {
            logError(e);
        }
    }

    @Override
    public void handleCommentDeleteMessage(CommentDeletedRabbitMessage message) {
        // 评论删除消息
    }

    public void handleCollectMessage(CollectRabbitMessage message) {
        try {
            int itemUser = message.getToUserId();
            if (Constants.ZERO.equals(message.getState())
                    || isSelf(message.getUserId(), itemUser)) {
                return;
            }
            //收藏消息 xx收藏了你的文章
            int articleId = message.getItemId();
            NotifyMsg msg = NotifyMsgBuilder
                    .buildCollectMsg(message.getUserId(), itemUser, articleId);
            save(msg);
        } catch (Exception e) {
            logError(e);
        }
    }

    @Override
    public void handleFollowMessage(FollowRabbitMessage message) {
        try {
            if (Constants.ZERO.equals(message.getState())) {
                return;
            }
            //xx 关注了你
            NotifyMsg msg = NotifyMsgBuilder
                    .buildFollowMsg(message.getUserId(), message.getToUserId());
            save(msg);
        } catch (Exception e) {
            logError(e);
        }
    }

    @RabbitHandler
    public void handleNotifyReadMessage(NotifyRabbitReadMessage message) {
        try {
            Integer receiver = message.getReceiver();
            List<Integer> types = message.getTypes();
            notifyMsgRepository.updateNotifyState(receiver, types);
        } catch (Exception e) {
            logError(e);
        }
    }

    @RabbitHandler
    public void handleUserCreateMessage(UserRabbitMessage message) {
        try {
            Integer receiver = message.getUserId();
            NotifyMsg msg = NotifyMsgBuilder.buildSystemMsg(receiver, -1, welcome);
            save(msg);
        } catch (Exception e) {
            logError(e);
        }
    }

    private void save(NotifyMsg msg) {
        notifyMsgRepository.saveNotifyMsg(msg);
    }

    private boolean isSelf(Integer sender, Integer receiver) {
        return Objects.equals(sender, receiver);
    }


    private void logError(Exception e) {
        log.error("error_class: {} error_msg: {}", e.getClass().getName(), e.getMessage());
    }
}
