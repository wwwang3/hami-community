package top.wang3.hami.common.message;

import lombok.Data;
import top.wang3.hami.common.constant.Constants;

@Data
public abstract class InteractRabbitMessage implements RabbitMessage {
    public InteractRabbitMessage() {

    }

    public InteractRabbitMessage(int userId, int toUserId, byte state, Integer itemId) {
        this.userId = userId;
        this.toUserId = toUserId;
        this.state = state;
        this.itemId = itemId;
    }

    /**
     * 行为: 点赞, 收藏, 关注
     * 发出行为的用户
     */
    private int userId;

    /**
     * 接收行为的用户
     */
    private int toUserId;

    /**
     * 状态 取消还是触发行为
     */
    private byte state;

    /**
     * 关联的实体ID(文章ID, 评论ID等)
     */
    private Integer itemId;

    @Override
    public String getExchange() {
        return RabbitMessage.super.getExchange();
    }

    public String getPrefix() {
        return Constants.ONE.equals(state) ? "do." : "cancel.";
    }
}
