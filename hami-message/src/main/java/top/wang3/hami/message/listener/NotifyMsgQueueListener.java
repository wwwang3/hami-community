package top.wang3.hami.message.listener;


import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.notify.*;
import top.wang3.hami.common.model.NotifyMsg;
import top.wang3.hami.common.model.UserFollow;
import top.wang3.hami.core.mapper.ArticleMapper;
import top.wang3.hami.core.mapper.CommentMapper;
import top.wang3.hami.core.service.common.NotifyMsgService;
import top.wang3.hami.core.service.user.UserFollowService;

import java.util.List;
import java.util.Objects;


@Component
@RabbitListener(messageConverter = "rabbitMQJacksonConverter",
        queues = Constants.NOTIFY_QUEUE, concurrency = "4")
@Slf4j
public class NotifyMsgQueueListener {


    @Resource
    NotifyMsgService notifyMsgService;

    @Resource
    UserFollowService userFollowService;

    @Resource
    ArticleMapper articleMapper;

    @Resource
    CommentMapper commentMapper;

    @PostConstruct
    public void init() {
        log.debug("rabbit listener NotifyMsgQueueListener register for use");
    }


    @RabbitHandler
    public void handleArticlePublishMsg(ArticlePublishMsg msg) {
        try {
            log.debug("receive msg: {}", msg);
            int authorId = msg.getAuthorId();
            //该作者的粉丝
            List<UserFollow> follows = ChainWrappers.queryChain(userFollowService.getBaseMapper())
                    .eq("following", authorId)
                    .eq("`state`", Constants.ONE)
                    .list();
            List<NotifyMsg> msgs = follows.stream().map(follower -> {
                NotifyMsg notifyMsg = new NotifyMsg();
                notifyMsg.setItemId(msg.getArticleId());
                notifyMsg.setItemName(msg.getTitle());
                notifyMsg.setRelatedId(msg.getArticleId());
                notifyMsg.setSender(msg.getAuthorId());
                notifyMsg.setReceiver(follower.getUserId());
                notifyMsg.setType(msg.getNotifyType());
                return notifyMsg;
            }).toList();
            notifyMsgService.saveBatch(msgs);
        } catch (Exception e) {
            //先忽略
            e.printStackTrace();
            logError(e);
        }
    }

    @RabbitHandler
    public void handleFollowMsg(FollowMsg followMsg) {
        try {
            //关注消息
            int notifyType = followMsg.getNotifyType();
            int userId = followMsg.getUserId(); //用户
            int following = followMsg.getFollowingId(); //被关注用户
            if (notifyMsgService.checkExist(userId, following, notifyType)) {
                return;
            }
            NotifyMsg msg = NotifyMsg.builder()
                    .itemId(userId)
                    .itemName("关注")
                    .relatedId(userId)
                    .sender(userId)
                    .receiver(following)
                    .type(notifyType)
                    .build();
            notifyMsgService.saveMsg(msg);
        } catch (Exception e) {
            logError(e);
        }
    }

    @RabbitHandler
    public void handleLikeMsg(LikeMsg msg) {
        try {
            int notifyType = msg.getNotifyType();
            int itemId = msg.getItemId();
            int sender = msg.getLikerId();
            Integer receiver = getReceiver(msg);
            if (receiver == sender) return;
            if (notifyMsgService.checkExist(sender, receiver, notifyType)) return;
            NotifyMsg notifyMsg = NotifyMsg.builder()
                    .itemId(itemId)
                    .relatedId(itemId) //xx赞了你的文章
                    .sender(sender)
                    .receiver(receiver)
                    .type(notifyType)
                    .build();
            notifyMsgService.saveMsg(notifyMsg);
        } catch (Exception e) {
            e.printStackTrace();
            logError(e);
        }
    }

    @RabbitHandler
    public void handleCommentMsg(CommentMsg commentMsg) {
        try {
            //作者自己发的评论不要通知
            int sender = commentMsg.getUserId();
            int receiver = commentMsg.getCommentTo();
            if (sender == receiver) return;
            NotifyMsg notifyMsg = NotifyMsg.builder()
                    .itemId(commentMsg.getArticleId())
                    .relatedId(commentMsg.getCommentId())
                    .sender(sender) //谁评论的
                    .receiver(receiver)
                    .type(commentMsg.getNotifyType())
                    .detail(commentMsg.getContent())
                    .build();
            notifyMsgService.saveMsg(notifyMsg);
        } catch (Exception e) {
            logError(e);
        }
    }

    @RabbitHandler
    public void handleReply(ReplyMsg msg) {
        try {
            int receiver = msg.getReplyTo();
            int sender = msg.getUserId();
            if (receiver == sender) return; //自己回复自己
            NotifyMsg notifyMsg = NotifyMsg.builder()
                    .itemId(msg.getArticleId())
                    .relatedId(msg.getReplyId())
                    .sender(msg.getUserId())
                    .receiver(receiver)
                    .detail(msg.getContent())
                    .type(msg.getNotifyType())
                    .build();
            notifyMsgService.saveMsg(notifyMsg);
        } catch (Exception e) {
            logError(e);
        }
    }

    @RabbitHandler
    public void handleCollect(CollectMsg msg) {
        try {
            //收藏
            int userId = msg.getUserId();
            int articleId = msg.getArticleId();
            int type = msg.getNotifyType();
            Integer receiver = getAuthorId(userId, articleId, type);
            NotifyMsg notifyMsg = NotifyMsg.builder()
                    .itemId(articleId)
                    .relatedId(articleId) //x收藏了你的文章
                    .sender(userId)
                    .receiver(receiver)
                    .type(type)
                    .build();
            notifyMsgService.saveMsg(notifyMsg);
        } catch (Exception e) {
            logError(e);
        }
    }

    private void logError(Exception e) {
        log.error("failed to process message, error_class: {}, error_msg: {}", e.getClass(), e.getMessage());
    }

    private Integer getAuthorId(Integer sender, Integer articleId, Integer type) {
        Integer authorId = articleMapper.getArticleAuthorId(articleId);
        if (Objects.equals(sender, authorId)) return null;
        boolean exist = notifyMsgService.checkExist(sender, authorId, type);
        if (!exist) return authorId;
        return null;
    }

    private Integer getReceiver(LikeMsg msg) {
        if (msg.getNotifyType() == NotifyType.ARTICLE_LIKE.getType()) {
            return articleMapper.getArticleAuthor(msg.getItemId());
        } else {
            return commentMapper.getCommentUserById(msg.getItemId());
        }
    }
}
