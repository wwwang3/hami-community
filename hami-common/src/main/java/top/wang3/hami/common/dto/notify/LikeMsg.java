package top.wang3.hami.common.dto.notify;


import lombok.Data;

@Data
public class LikeMsg {

    /**
     * 点赞人ID
     */
    int likerId;
    int itemId;
    int itemType;
}
