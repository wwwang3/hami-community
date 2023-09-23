package top.wang3.hami.core.service.notify.consumer;


import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.article.ArticleDTO;
import top.wang3.hami.common.dto.builder.NotifyMsgBuilder;
import top.wang3.hami.common.dto.notify.NotifyType;
import top.wang3.hami.common.message.*;
import top.wang3.hami.common.model.Comment;
import top.wang3.hami.common.model.NotifyMsg;
import top.wang3.hami.core.repository.CommentRepository;
import top.wang3.hami.core.repository.NotifyMsgRepository;
import top.wang3.hami.core.service.article.ArticleService;

import java.util.Objects;

@RabbitListener(bindings = {
        @QueueBinding(
                value = @Queue("hami-notify-queue"),
                exchange = @Exchange(value = Constants.HAMI_TOPIC_EXCHANGE1, type = "topic"),
                key = {"*.follow", "do.like.*", "*.collect", "comment.comment", "comment.reply"}
        )
})
@Component
@RequiredArgsConstructor
@Slf4j
public class NotifyMsgConsumer {

    private final ArticleService articleService;
    private final CommentRepository commentRepository;
    private final NotifyMsgRepository notifyMsgRepository;

    @Resource
    TransactionTemplate transactionTemplate;

    //点赞 评论 收藏 关注等通知消息
    @RabbitHandler
    public void handleLikeMessage(LikeRabbitMessage message) {
        //点赞消息
        try {
            //点赞一般需要判断取消后再次点赞的, 数据库加了索引不管了
            if (!message.isState()) {
                return;
            }
            if (Constants.LIKE_TYPE_ARTICLE.equals(message.getType())) {
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
        ArticleDTO article = articleService.getArticleDTOById(message.getItemId());
        Integer sender = message.getUserId();
        if (article == null || isSelf(sender, article.getUserId())) {
            return;
        }
        NotifyMsg msg = NotifyMsgBuilder
                .buildArticleLikeMsg(message.getUserId(), article.getUserId(), message.getItemId());
        notifyMsgRepository.save(msg);
    }

    private void handleCommentLike(LikeRabbitMessage message) {
        //评论点赞 xx赞了你的评论
        Integer commentId = message.getItemId();
        Comment comment = commentRepository.getById(commentId);
        if (comment == null || isSelf(comment.getUserId(), message.getUserId())) {
            return;
        }
        NotifyMsg msg = NotifyMsgBuilder.buildCommentLikerMsg(message.getUserId(),
                comment.getUserId(), commentId, comment.getArticleId(), comment.getContent());
        notifyMsgRepository.save(msg);
    }

    @RabbitHandler
    public void handleCommentMessage(CommentRabbitMessage message) {
        try {
            //评论文章通知 xx评论了你的文章
            Integer articleId = message.getArticleId();
            ArticleDTO article = articleService.getArticleDTOById(message.getArticleId());
            if (article == null) return;
            if (isSelf(article.getUserId(), message.getUserId())) {
                return;
            }
            NotifyMsg msg = NotifyMsgBuilder.buildCommentMsg(message.getUserId(), article.getUserId(),
                    message.getCommentId(), articleId, message.getDetail());
            save(msg);
        } catch (Exception e) {
            logError(e);
        }
    }

    @RabbitHandler
    public void handleReplyMessage(ReplyRabbitMessage message) {
        try {
            Integer articleId = message.getArticleId();
            ArticleDTO article = articleService.getArticleDTOById(articleId);
            if (article == null) return;
            Integer articleAuthor = article.getUserId();
            //把他爹也查出来 内容一起写入通知 xx回复了你的评论
            //自己回复自己不需要通知
            Comment comment = commentRepository.getById(message.getParentId());
            if (comment == null || isSelf(message.getUserId(), comment.getUserId())) {
                return;
            }
            String split = "##@xx-"; //todo 用户评论可能含有分隔符
            String detail = comment.getContent() + split + message.getDetail();
            NotifyMsg msg = NotifyMsgBuilder
                    .buildReplyMsg(
                            message.getUserId(), articleAuthor,
                            message.getReplyId(), message.getArticleId(),
                            detail
                    );
            save(msg);
        } catch (Exception e) {
            logError(e);
        }
    }

    @RabbitHandler
    public void handleCollectMessage(CollectRabbitMessage message) {
        try {
            if (!message.isState()) {
                return;
            }
            //收藏消息 xx收藏了你的文章
            int articleId = message.getArticleId();
            ArticleDTO article = articleService.getArticleDTOById(articleId);
            if (article == null || isSelf(message.getUserId(), article.getUserId())) {
                return;
            }
            NotifyMsg msg = NotifyMsgBuilder
                    .buildCollectMsg(
                            message.getUserId(), article.getUserId(),
                            articleId
                    );
            save(msg);
        } catch (Exception e) {
            logError(e);
        }
    }

    @RabbitHandler
    public void handleFollowMsg(FollowRabbitMessage rabbitMessage) {
        try {
            if (!rabbitMessage.isState()){
                return;
            }
            //xx 关注了你
            NotifyMsg msg = NotifyMsgBuilder
                    .buildFollowMsg(rabbitMessage.getUserId(), rabbitMessage.getFollowing());
            save(msg);
        } catch (Exception e) {
            logError(e);
        }
    }

    private void save(NotifyMsg msg) {
        transactionTemplate.execute(status -> {
            notifyMsgRepository.save(msg);
            return null;
        });
    }

    private boolean isSelf(Integer sender, Integer receiver) {
        return Objects.equals(sender, receiver);
    }

    private boolean checkExist(Integer itemId, Integer sender, Integer receiver, NotifyType type) {
        return notifyMsgRepository.checkExist(itemId, sender, receiver, type);
    }
    private void logError(Exception e) {
        log.debug("error_class: {} error_msg: {}", e.getClass().getName(), e.getMessage());
    }
}
