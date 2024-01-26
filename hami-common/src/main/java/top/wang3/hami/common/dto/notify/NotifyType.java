package top.wang3.hami.common.dto.notify;

import lombok.Getter;

@Getter
public enum NotifyType {

    /**
     * 系统消息
     */
    SYSTEM(0),

    /**
     * 评论消息
     */
    COMMENT(1),

    /**
     * 评论回复消息
     */
    REPLY(2),

    /**
     * 文章点赞消息
     */
    ARTICLE_LIKE(3),

    /**
     * 评论点赞消息
     */
    COMMENT_LIKE(4),

    /**
     * 收藏消息
     */
    COLLECT(5),

    /**
     * 关注消息
     */
    FOLLOW(6),

    /**
     * 文章发表消息
     */
    PUBLISH_ARTICLE(7);

    /**
     * 类型 from 0 to 7
     */
    final int type;

    NotifyType(int type) {
        this.type = type;
    }
}
