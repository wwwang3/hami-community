package top.wang3.hami.message.listener;


import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.dto.notify.ArticlePublishMsg;
import top.wang3.hami.common.model.NotifyMsg;
import top.wang3.hami.common.model.UserFollow;
import top.wang3.hami.core.service.common.NotifyMsgService;
import top.wang3.hami.core.service.user.UserFollowService;

import java.util.List;


@Component
@RabbitListener(messageConverter = "rabbitMQJacksonConverter", queues = Constants.NOTIFY_QUEUE)
@Slf4j
public class NotifyMsgQueueListener {


    @Resource
    NotifyMsgService notifyMsgService;

    @Resource
    UserFollowService userFollowService;

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
                    .eq("`state`", 0)
                    .list();
            List<NotifyMsg> msgs = follows.stream().map(follower -> {
                NotifyMsg notifyMsg = new NotifyMsg();
                notifyMsg.setItemId(msg.getArticleId());
                notifyMsg.setItemName(msg.getTitle());
                notifyMsg.setRelatedId(authorId);
                notifyMsg.setSender(msg.getAuthorId());
                notifyMsg.setReceiver(follower.getUserId());
                return notifyMsg;
            }).toList();
            notifyMsgService.saveBatch(msgs);
        } catch (Exception e) {
            //先忽略
            log.warn("failed to process message, error_class: {}, error_msg: {}", e.getClass(), e.getMessage());
        }
    }
}
