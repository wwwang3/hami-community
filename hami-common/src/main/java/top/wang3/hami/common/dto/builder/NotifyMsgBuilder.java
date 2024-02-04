package top.wang3.hami.common.dto.builder;

import lombok.Getter;
import top.wang3.hami.common.dto.notify.NotifyType;
import top.wang3.hami.common.model.NotifyMsg;

@Getter
public class NotifyMsgBuilder {

    private Integer relatedId = 0; //关联的ID 文章ID或者评论ID
    private Integer itemId; //触发消息的ID 文章ID或者评论ID
    private Integer sender; //消息发送者 (用户)
    private Integer receiver; //消息接收者
    private NotifyType type; //消息类型
    private String detail = ""; //内容

    public NotifyMsgBuilder sender(Integer sender) {
        this.sender = sender;
        return this;
    }

    public NotifyMsgBuilder receiver(Integer receiver) {
        this.receiver = receiver;
        return this;
    }

    public NotifyMsgBuilder relatedId(Integer relatedId) {
        this.relatedId = relatedId;
        return this;
    }

    public NotifyMsgBuilder itemId(Integer itemId) {
        this.itemId = itemId;
        return this;
    }

    public NotifyMsgBuilder type(NotifyType type) {
        this.type = type;
        return this;
    }

    public NotifyMsgBuilder detail(String detail) {
        this.detail = detail;
        return this;
    }


    public static NotifyMsg buildFollowMsg(Integer userId, Integer following) {
        //新增粉丝
        return new NotifyMsgBuilder()
                .sender(userId)
                .itemId(userId)
                .receiver(following)
                .type(NotifyType.FOLLOW)
                .build();
    }


    public static NotifyMsg buildArticleLikeMsg(Integer userId, Integer authorId, Integer articleId) {
        //点赞文章消息
        return new NotifyMsgBuilder()
                .type(NotifyType.ARTICLE_LIKE)
                .sender(userId)
                .receiver(authorId)
                .itemId(articleId)
                .relatedId(articleId)
                .build();
    }

    public static NotifyMsg buildCommentLikerMsg(Integer userId, Integer commentUser,
                                                 Integer commentId, Integer articleId, String detail) {
        //评论点赞消息
        return new NotifyMsgBuilder()
                .sender(userId)
                .receiver(commentUser)
                .itemId(commentId)
                .relatedId(articleId)
                .type(NotifyType.COMMENT_LIKE)
                .detail(detail)
                .build();
    }

    public static NotifyMsg buildCollectMsg(Integer userId, Integer articleAuthor, Integer articleId) {
        return new NotifyMsgBuilder()
                .sender(userId)
                .receiver(articleAuthor)
                .itemId(articleId)
                .relatedId(articleId) //关联的ID一般都为articleId
                .type(NotifyType.COLLECT)
                .build();
    }

    public static NotifyMsg buildCommentMsg(Integer userId, Integer articleAuthor,
                                            Integer commentId, Integer articleId, String detail) {
        //文章评论消息
        return new NotifyMsgBuilder()
                .sender(userId)
                .receiver(articleAuthor)
                .itemId(commentId)
                .relatedId(articleId) //评论关联的文章
                .type(NotifyType.COMMENT)
                .detail(detail) //评论发布了一般不可以修改
                .build();
    }

    public static NotifyMsg buildReplyMsg(Integer userId, Integer replyTo, Integer replyId,
                                          Integer articleId, String detail) {
        //评论回复消息
        return new NotifyMsgBuilder()
                .sender(userId)
                .receiver(replyTo) //userId回复的谁
                .itemId(replyId) //回复Id
                .relatedId(articleId)
                .type(NotifyType.REPLY)
                .detail(detail)
                .build();
    }

    public static NotifyMsg buildSystemMsg(Integer receiver, Integer itemId, String detail) {
        return new NotifyMsgBuilder()
                .sender(1)
                .receiver(receiver)
                .itemId(itemId)
                .type(NotifyType.SYSTEM)
                .detail(detail)
                .build();

    }

    public NotifyMsg build() {
        NotifyMsg notifyMsg = new NotifyMsg();
        notifyMsg.setItemId(itemId);
        notifyMsg.setReceiver(receiver);
        notifyMsg.setRelatedId(relatedId);
        notifyMsg.setSender(sender);
        notifyMsg.setType(type.getType());
        notifyMsg.setDetail(detail);
        return notifyMsg;
    }

}
