package top.wang3.hami.core.service.stat.consumer;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.message.*;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.component.InteractConsumer;
import top.wang3.hami.core.service.article.repository.ArticleRepository;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@RabbitListener(bindings = {
        @QueueBinding(
                value = @Queue(value = "hami-data-growing-queue-1"),
                exchange = @Exchange(value = Constants.HAMI_TOPIC_EXCHANGE1, type = "topic"),
                key = {"*.follow", "*.like.*", "*.collect", "comment.comment", "comment.reply", "comment.delete"}
        ),
        @QueueBinding(
                value = @Queue(value = "hami-data-growing-queue-2"),
                exchange = @Exchange(value = Constants.HAMI_TOPIC_EXCHANGE2, type = "topic"),
                key = {"article.publish", "article.delete"}
        )
})
@RequiredArgsConstructor
public class DataGrowingConsumer implements InteractConsumer {

    private final ArticleRepository articleRepository;

    @Override
    @RabbitHandler
    public void handleFollowMessage(FollowRabbitMessage message) {
        //关注消息
        String key = buildKey(message.getToUserId());
        if (Constants.ONE.equals(message.getState())) {
            //新增关注
            RedisClient.hIncr(key, Constants.DATA_GROWING_FOLLOWER, 1);
        } else {
            //取消关注
            RedisClient.hIncr(key, Constants.DATA_GROWING_CANCEL_FOLLOW, 1);
        }
        ensureExpireTime(key);
    }

    @Override
    @RabbitHandler
    public void handleCollectMessage(CollectRabbitMessage message) {
        String key = buildKey(message.getToUserId());
        if (Constants.ONE.equals(message.getState())) {
            RedisClient.hIncr(key, Constants.DATA_GROWING_COLLECT, 1);
        } else {
            RedisClient.hIncr(key, Constants.DATA_GROWING_COLLECT, -1);
        }
        ensureExpireTime(key);
    }

    @RabbitHandler
    public void handleLikeMessage(LikeRabbitMessage message) {
        String key = buildKey(message.getToUserId());
        if (Constants.ONE.equals(message.getState())) {
            RedisClient.hIncr(key, Constants.DATA_GROWING_LIKE, 1);
        } else {
            RedisClient.hIncr(key, Constants.DATA_GROWING_LIKE, -1);
        }
        ensureExpireTime(key);
    }

    @RabbitHandler
    public void handleCommentMessage(CommentRabbitMessage message) {
        String key = buildKey(message.getAuthorId());
        RedisClient.hIncr(key, Constants.DATA_GROWING_COMMENT, 1);
        ensureExpireTime(key);
    }

    @Override
    public void handleCommentDeleteMessage(CommentDeletedRabbitMessage message) {
        Integer author = articleRepository.getArticleAuthor(message.getArticleId());
        String key = buildKey(author);
        RedisClient.hIncr(key, Constants.DATA_GROWING_COMMENT, -1);
        ensureExpireTime(key);
    }

    @RabbitHandler
    public void handleArticleMessage(ArticleRabbitMessage message) {
        Integer authorId = message.getAuthorId();
        String key = buildKey(authorId);
        if (ArticleRabbitMessage.Type.DELETE.equals(message.getType())) {
            RedisClient.hIncr(key, Constants.DATA_GROWING_ARTICLE, -1);
        } else if (ArticleRabbitMessage.Type.PUBLISH.equals(message.getType())) {
            RedisClient.hIncr(key, Constants.DATA_GROWING_ARTICLE, 1);
        }
        ensureExpireTime(key);
    }

    private String buildKey(Integer userId) {
        String date = DateUtil.formatDate(new Date());
        return Constants.DATA_GROWING + date + ":" + userId;
    }

    private void ensureExpireTime(String key) {
        long expire = RedisClient.getExpire(key);
        if (expire == -1) {
            //没有设置就设置
            DateTime endOfDay = DateUtil.endOfDay(new Date());
            long timeout = endOfDay.getTime() - System.currentTimeMillis();
            RedisClient.expire(key, timeout, TimeUnit.MILLISECONDS);
        }
    }
}
