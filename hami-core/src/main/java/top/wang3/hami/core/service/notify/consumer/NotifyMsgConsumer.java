package top.wang3.hami.core.service.notify.consumer;


import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.article.ArticleInfo;
import top.wang3.hami.common.dto.builder.NotifyMsgBuilder;
import top.wang3.hami.common.message.*;
import top.wang3.hami.common.model.Comment;
import top.wang3.hami.common.model.NotifyMsg;
import top.wang3.hami.core.repository.CommentRepository;
import top.wang3.hami.core.repository.NotifyMsgRepository;
import top.wang3.hami.core.service.article.ArticleService;

@RabbitListener(bindings = {
        @QueueBinding(
                value = @Queue("hami-user-interact-queue-3"),
                exchange = @Exchange(value = Constants.HAMI_TOPIC_EXCHANGE1, type = "topic"),
                key = {"*.follow", "do.like.*", "*.collect", "comment.comment", "comment.reply"}
        )
})
@Component
@RequiredArgsConstructor
public class NotifyMsgConsumer {

    private final ArticleService articleService;
    private final CommentRepository commentRepository;
    private final NotifyMsgRepository notifyMsgRepository;

    @Resource
    TransactionTemplate transactionTemplate;

    //点赞 评论 收藏 关注等通知消息
    @RabbitListener
    public void handleLikeMessage(LikeRabbitMessage message) {
        //点赞消息
        if (!message.isState()) {
            return;
        }
        if (Constants.LIKE_TYPE_ARTICLE.equals(message.getType())) {
            //文章点赞
            handleArticleLike(message);
        } else {
            handleCommentLike(message);
        }
    }

    private void handleArticleLike(LikeRabbitMessage message) {
        //点赞通知 xx赞了你的文章
        ArticleInfo article = articleService.getArticleInfoById(message.getItemId());
        NotifyMsg msg = NotifyMsgBuilder
                .buildArticleLikeMsg(message.getUserId(), message.getItemId(), article.getUserId());
        notifyMsgRepository.save(msg);
    }

    private void handleCommentLike(LikeRabbitMessage message) {
        //评论点赞 xx赞了你的评论
        Integer commentId = message.getItemId();
        Comment comment = commentRepository.getById(commentId);
        if (comment == null) return;
        NotifyMsg msg = NotifyMsgBuilder.buildCommentLikerMsg(message.getUserId(),
                comment.getUserId(), commentId, comment.getArticleId(), comment.getContent());
        notifyMsgRepository.save(msg);
    }

    @RabbitHandler
    public void handleCommentMessage(CommentRabbitMessage message) {
        //评论文章通知 xx评论了你的文章
        Integer articleId = message.getArticleId();
        ArticleInfo article = articleService.getArticleInfoById(message.getArticleId());
        if (article == null) return;
        NotifyMsg msg = NotifyMsgBuilder.buildCommentMsg(message.getUserId(), article.getUserId(),
                message.getCommentId(), articleId, message.getDetail());
        save(msg);
    }

    @RabbitHandler
    public void handleReplyMessage(ReplyRabbitMessage message) {
        Integer articleId = message.getArticleId();
        ArticleInfo article = articleService.getArticleInfoById(articleId);
        Integer articleAuthor = article.getUserId();
        //把他爹也查出来 内容一起写入通知 xx回复了你的评论
        Comment comment = commentRepository.getById(message.getParentId());
        String split = "##@xx-"; //todo 用户评论可能含有分隔符
        String detail = comment.getContent() + split + message.getDetail();
        NotifyMsg msg = NotifyMsgBuilder
                .buildReplyMsg(
                        message.getUserId(), articleAuthor,
                        message.getReplyId(), message.getArticleId(),
                        detail
                );
        save(msg);
    }

    @RabbitHandler
    public void handleCollectMessage(CollectRabbitMessage message) {
        if (!message.isState()) {
            return;
        }
        //收藏消息 xx收藏了你的文章
        int articleId = message.getArticleId();
        ArticleInfo article = articleService.getArticleInfoById(articleId);
        NotifyMsg msg = NotifyMsgBuilder
                .buildCollectMsg(
                        message.getUserId(), article.getUserId(),
                        articleId
                );
        save(msg);
    }

    @RabbitHandler
    public void handleFollowMsg(FollowRabbitMessage rabbitMessage) {
        if (!rabbitMessage.isState()){
            return;
        }
        //xx 关注了你
        NotifyMsg msg = NotifyMsgBuilder
                .buildFollowMsg(rabbitMessage.getUserId(), rabbitMessage.getFollowing());
       save(msg);
    }

    private void save(NotifyMsg msg) {
        transactionTemplate.execute(status -> {
            notifyMsgRepository.save(msg);
            return null;
        });
    }
}
