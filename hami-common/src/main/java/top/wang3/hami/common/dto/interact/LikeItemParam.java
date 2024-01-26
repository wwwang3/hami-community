package top.wang3.hami.common.dto.interact;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 点赞请求参数
 */
@Data
public class LikeItemParam {

    /**
     * 点赞的实体Id
     */
    @JsonProperty("item_id")
    private int itemId;

    /**
     * 实体类型
     * @see top.wang3.hami.common.dto.notify.NotifyType
     */
    @JsonProperty("item_type")
    private byte itemType;
}
