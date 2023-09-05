package top.wang3.hami.common.dto.notify;

public enum NotifyType {
    SYSTEM(0, "系统消息"),
    COMMENT(1, "评论消息"),
    REPLY(2, "评论回复消息"),
    LIKE(3, "点赞消息"),
    COLLECT(4, "收藏消息"),
    FOLLOW(5, "关注消息"),
    PUBLISH_ARTICLE(6, "发布文章消息");

    final int type;
    final String msg;

    NotifyType(int type, String msg) {
        this.type = type;
        this.msg = msg;
    }
    /**
     * 通知类型: 0-系统，1-评论，2-回复 3-点赞 4-收藏 5-关注 6-关注的用户发布新文章
     */
    public static final int NOTIFY_TYPE_SYSTEM = 0;
    public static final int NOTIFY_TYPE_COMMENT = 1;
    public static final int NOTIFY_TYPE_REPLY = 2;
    public static final int NOTIFY_TYPE_LIKE = 3;
    public static final int NOTIFY_TYPE_COLLECT = 4;
    public static final int NOTIFY_TYPE_FOLLOW = 5;
    public static final int NOTIFY_TYPE_NEW_ARTICLE = 6;

}
