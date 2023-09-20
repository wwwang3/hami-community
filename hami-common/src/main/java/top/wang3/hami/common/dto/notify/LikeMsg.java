package top.wang3.hami.common.dto.notify;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wang3.hami.common.constant.Constants;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LikeMsg implements Notify {

    /**
     * 点赞人ID
     */
    int likerId;
    int itemId;
    byte itemType;
    @Override
    public int getNotifyType() {
        return Constants.LIKE_TYPE_ARTICLE.equals(itemType) ? NotifyType.ARTICLE_LIKE.getType() :
                NotifyType.COMMENT_LIKE.getType();
    }
}
