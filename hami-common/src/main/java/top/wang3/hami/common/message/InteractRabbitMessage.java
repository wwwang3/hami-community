package top.wang3.hami.common.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wang3.hami.common.constant.Constants;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class InteractRabbitMessage implements RabbitMessage {

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
