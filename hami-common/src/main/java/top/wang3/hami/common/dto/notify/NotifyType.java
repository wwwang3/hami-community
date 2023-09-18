package top.wang3.hami.common.dto.notify;

import lombok.Getter;

@Getter
public enum NotifyType {
    SYSTEM(0, "系统消息"),
    COMMENT(1, "评论消息"),
    REPLY(2, "评论回复消息"),
    ARTICLE_LIKE(3, "文章点赞"),
    COMMENT_LIKE(4, "评论点赞"),
    COLLECT(5, "收藏消息"),
    FOLLOW(6, "关注消息"),
    PUBLISH_ARTICLE(7, "发布文章消息");

    final int type;
    final String msg;

    NotifyType(int type, String msg) {
        this.type = type;
        this.msg = msg;
    }
}
