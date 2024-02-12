package top.wang3.hami.common.message.interact;

import lombok.Data;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.constant.RabbitConstants;
import top.wang3.hami.common.message.RabbitMessage;

@Data
public abstract class InteractRabbitMessage implements RabbitMessage {

    /**
     * 行为: 点赞, 收藏, 关注
     * 发出行为的用户
     */
    private int userId;

    /**
     * 接收行为的用户 maybe null
     */
    private Integer toUserId;

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
        return RabbitConstants.HAMI_INTERACT_EXCHANGE;
    }

    public String getPrefix() {
        return Constants.ONE.equals(state) ? "do." : "cancel.";
    }

    public InteractRabbitMessage() {

    }

    public InteractRabbitMessage(int userId, Integer toUserId, byte state, Integer itemId) {
        this.userId = userId;
        this.toUserId = toUserId;
        this.state = state;
        this.itemId = itemId;
    }
}
