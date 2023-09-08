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
import top.wang3.hami.core.service.common.NotifyMsgService;
import top.wang3.hami.core.service.user.UserFollowService;

import java.util.List;


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
            int likerId = msg.getLikerId();
            int itemId = msg.getItemId();
            Integer authorId = articleMapper.getArticleAuthorId(itemId);
            //文章点赞/评论点赞
            //要是评论ID和文章ID重复咋办
            if (notifyMsgService.checkExist(likerId, itemId, notifyType)) {
                //对该文章点过赞
                //用户取消赞也不管了
                return;
            }
            NotifyMsg notifyMsg = NotifyMsg.builder()
                    .relatedId(itemId) //xx赞了你的文章
                    .sender(likerId)
                    .receiver(authorId)
                    .type(notifyType)
                    .build();
            notifyMsgService.saveMsg(notifyMsg);
        } catch (Exception e) {
            logError(e);
        }
    }

    @RabbitHandler
    public void handleCommentMsg(CommentMsg commentMsg) {
        try {
            NotifyMsg notifyMsg = NotifyMsg.builder()
                    .itemId(commentMsg.getArticleId())
                    .relatedId(commentMsg.getCommentId())
                    .sender(commentMsg.getUserId()) //谁评论的
                    .receiver(commentMsg.getCommentTo())
                    .type(commentMsg.getNotifyType())
                    .detail(commentMsg.getContent()) //todo 感觉冗余用户信息等也可以，查询还得连表
                    .build();
            notifyMsgService.saveMsg(notifyMsg);
        } catch (Exception e) {
            logError(e);
        }
    }

    @RabbitHandler
    public void handleReply(ReplyMsg msg) {
        try {
            NotifyMsg notifyMsg = NotifyMsg.builder()
                    .itemId(msg.getArticleId())
                    .relatedId(msg.getArticleId())
                    .sender(msg.getUserId())
                    .receiver(msg.getReplyTo())
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
            Integer authorId = articleMapper.getArticleAuthorId(articleId);
            if (notifyMsgService.checkExist(userId, authorId, type)) {
                return;
            }
            NotifyMsg notifyMsg = NotifyMsg.builder()
                    .relatedId(articleId) //x收藏了你的文章
                    .sender(userId)
                    .receiver(authorId)
                    .type(type)
                    .build();
            notifyMsgService.saveMsg(notifyMsg);
        } catch (Exception e) {
            logError(e);
        }
    }

    private void logError(Exception e) {
        log.warn("failed to process message, error_class: {}, error_msg: {}", e.getClass(), e.getMessage());
    }
}
