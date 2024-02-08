package top.wang3.hami.common.dto.interact;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 点赞请求参数
 */
@Data
public class LikeItemParam {

    /**
     * 点赞的实体Id
     */
    @NotNull
    @Min(value = 1)
    private int itemId;

    /**
     * 实体类型
     * @see top.wang3.hami.common.dto.interact.LikeType
     * @default 1
     */
    @NotNull
    private byte itemType;
}
